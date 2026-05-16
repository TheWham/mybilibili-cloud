import json
import logging
import os
import site
import sys
import time
from datetime import datetime
from pathlib import Path

import ollama
import redis
from elasticsearch import Elasticsearch, helpers


QUEUE_KEY = os.getenv("EASYLIVE_AI_QUEUE", "easylive:queue:ai:subtitle-vector")
DEAD_QUEUE_KEY = os.getenv("EASYLIVE_AI_DEAD_QUEUE", "easylive:queue:ai:subtitle-vector:dead")
STATUS_KEY_PREFIX = os.getenv("EASYLIVE_AI_STATUS_PREFIX", "easylive:ai:subtitle-vector:status:")
REDIS_URL = os.getenv("EASYLIVE_REDIS_URL", "redis://127.0.0.1:6379/0")
ES_URL = os.getenv("EASYLIVE_ES_URL", "http://127.0.0.1:9201")
ES_INDEX = os.getenv("EASYLIVE_ES_INDEX", "easylive_video_subtitle_vector")
# 默认直接使用标准版 bge-m3 模型名。
# Ollama 会在真正生成 embedding 时按需加载模型，是否继续占用显存由 keep_alive 决定。
EMBEDDING_MODEL = os.getenv("EASYLIVE_EMBEDDING_MODEL", "bge-m3:567m")
WHISPER_MODEL = os.getenv("EASYLIVE_WHISPER_MODEL", "small")
WHISPER_DEVICE = os.getenv("EASYLIVE_WHISPER_DEVICE", "cuda")
# 3050 Ti 这类 4GB 显卡更适合 int8_float16，能兼顾速度和显存占用。
WHISPER_COMPUTE_TYPE = os.getenv("EASYLIVE_WHISPER_COMPUTE_TYPE", "int8_float16")
REDIS_BLOCK_SECONDS = int(os.getenv("EASYLIVE_REDIS_BLOCK_SECONDS", "5"))
MAX_RETRY_COUNT = int(os.getenv("EASYLIVE_AI_MAX_RETRY_COUNT", "3"))
EXTRA_NVIDIA_DLL_DIRS = os.getenv("EASYLIVE_NVIDIA_DLL_DIRS", "")


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger("easylive-ai-worker")
_REGISTERED_DLL_DIRS = set()


def add_dll_search_path(path):
    path_obj = Path(path).expanduser()
    if not path_obj.exists():
        logger.warning("NVIDIA DLL 目录不存在, path=%s", path_obj)
        return

    path_text = str(path_obj)
    if path_text in _REGISTERED_DLL_DIRS:
        return

    # Windows 加载 cublas/cudnn 时只看当前进程的 DLL 搜索路径。
    # 本机装过 CUDA 或 pip 包不代表当前 venv 能找到它，所以这里显式补进来。
    os.environ["PATH"] = path_text + os.pathsep + os.environ["PATH"]
    if hasattr(os, "add_dll_directory"):
        os.add_dll_directory(path_text)
    _REGISTERED_DLL_DIRS.add(path_text)


def patch_nvidia_libs():
    """Windows 下让 faster-whisper 能找到 pip 或手动指定的 NVIDIA DLL。"""
    if EXTRA_NVIDIA_DLL_DIRS:
        for dll_dir in EXTRA_NVIDIA_DLL_DIRS.split(os.pathsep):
            if dll_dir.strip():
                add_dll_search_path(dll_dir.strip())

    site_package_candidates = set(site.getsitepackages())

    # 你的 demo 用的是全局 Python，它的 nvidia DLL 在 base_prefix 下面。
    # 当前 Worker 跑在 .venv 里，site.getsitepackages() 默认只会返回 .venv，
    # 所以这里额外扫一遍创建 venv 的基础 Python，避免同一台机器重复安装大包。
    for python_home in {sys.prefix, sys.base_prefix, os.path.dirname(sys.executable)}:
        site_package_candidates.add(os.path.join(python_home, "Lib", "site-packages"))

    for site_package in site_package_candidates:
        nvidia_base = os.path.join(site_package, "nvidia")
        if not os.path.exists(nvidia_base):
            continue

        for root, dirs, _files in os.walk(nvidia_base):
            if "bin" not in dirs:
                continue
            bin_path = os.path.join(root, "bin")
            add_dll_search_path(bin_path)


def is_cuda_runtime_error(error):
    message = str(error).lower()
    return (
        "cublas64_12.dll" in message
        or "cudnn" in message
        or ("cuda" in message and "library" in message)
    )


def load_whisper_model(model_class, device, compute_type):
    logger.info(
        "加载 Whisper 模型, model=%s, device=%s, computeType=%s",
        WHISPER_MODEL,
        device,
        compute_type,
    )
    return model_class(WHISPER_MODEL, device=device, compute_type=compute_type)


def build_redis_client():
    return redis.Redis.from_url(REDIS_URL, decode_responses=True)


def build_es_client():
    return Elasticsearch(ES_URL)


def now_text():
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def build_status_key(task):
    return STATUS_KEY_PREFIX + task["videoId"]


def mark_task_processing(redis_client, task):
    # video 服务只负责投递任务，worker 取到任务后立刻回写状态，后台列表才能看到真实进度。
    redis_client.hset(
        build_status_key(task),
        mapping={
            "status": "PROCESSING",
            "statusName": "处理中",
            "lastError": "",
            "updateTime": now_text(),
        },
    )


def mark_task_success(redis_client, task):
    status_key = build_status_key(task)
    success_count = redis_client.hincrby(status_key, "successCount", 1)
    task_count = int(redis_client.hget(status_key, "taskCount") or 0)
    update_mapping = {
        "lastError": "",
        "updateTime": now_text(),
    }
    if task_count > 0 and success_count >= task_count:
        update_mapping["status"] = "SUCCESS"
        update_mapping["statusName"] = "成功"
    redis_client.hset(status_key, mapping=update_mapping)


def mark_task_retry(redis_client, task, error, retry_count):
    redis_client.hset(
        build_status_key(task),
        mapping={
            "status": "PENDING",
            "statusName": "排队中",
            "retryCount": retry_count,
            "lastError": str(error)[:1000],
            "updateTime": now_text(),
        },
    )


def mark_task_failed(redis_client, task, error, retry_count):
    status_key = build_status_key(task)
    redis_client.hincrby(status_key, "failedCount", 1)
    redis_client.hset(
        status_key,
        mapping={
            "status": "FAILED",
            "statusName": "失败",
            "retryCount": retry_count,
            "lastError": str(error)[:1000],
            "updateTime": now_text(),
        },
    )


def requeue_or_dead_letter(redis_client, task_json, error):
    try:
        task = json.loads(task_json)
    except Exception:
        redis_client.lpush(
            DEAD_QUEUE_KEY,
            json.dumps(
                {
                    "rawTask": task_json,
                    "lastError": str(error)[:1000],
                    "updateTime": now_text(),
                },
                ensure_ascii=False,
            ),
        )
        logger.error("字幕向量化任务 JSON 非法，已进入死信队列, task=%s", task_json)
        return

    retry_count = int(task.get("retryCount") or 0) + 1
    task["retryCount"] = retry_count
    task["lastError"] = str(error)[:1000]
    task["updateTime"] = now_text()
    retry_task_json = json.dumps(task, ensure_ascii=False)

    if retry_count <= MAX_RETRY_COUNT:
        mark_task_retry(redis_client, task, error, retry_count)
        redis_client.lpush(QUEUE_KEY, retry_task_json)
        logger.warning(
            "字幕向量化任务失败，已重新入队, videoId=%s, fileId=%s, retry=%s/%s",
            task.get("videoId"),
            task.get("fileId"),
            retry_count,
            MAX_RETRY_COUNT,
        )
        return

    mark_task_failed(redis_client, task, error, retry_count)
    redis_client.lpush(DEAD_QUEUE_KEY, retry_task_json)
    logger.error(
        "字幕向量化任务超过最大重试次数，已进入死信队列, videoId=%s, fileId=%s, retry=%s",
        task.get("videoId"),
        task.get("fileId"),
        retry_count,
    )


def get_text_embedding(text):
    response = ollama.embeddings(model=EMBEDDING_MODEL, prompt=text)
    vector = response.get("embedding")
    if not vector:
        raise RuntimeError("Ollama 没有返回 embedding")
    return [round(float(value), 5) for value in vector]


def transcribe_and_embed(model, task):
    source_video = Path(task["sourceVideoPath"])
    if not source_video.exists():
        raise FileNotFoundError(f"源视频不存在: {source_video}")

    logger.info("开始提取字幕, videoId=%s, fileId=%s, path=%s", task["videoId"], task["fileId"], source_video)
    segments, info = model.transcribe(str(source_video), beam_size=5)
    logger.info(
        "字幕识别语言=%s, 置信度=%.2f, videoId=%s, fileId=%s",
        info.language,
        info.language_probability,
        task["videoId"],
        task["fileId"],
    )

    docs = []
    for segment_index, segment in enumerate(segments):
        text = segment.text.strip()
        if not text:
            continue

        vector = get_text_embedding(text)
        docs.append(
            {
                "_index": ES_INDEX,
                "_id": f"{task['videoId']}_{task['fileId']}_{segment_index}",
                "_source": {
                    "videoId": task["videoId"],
                    "fileId": task["fileId"],
                    "userId": task.get("userId"),
                    "fileIndex": task.get("fileIndex"),
                    "segmentIndex": segment_index,
                    "videoName": task.get("videoName"),
                    "videoCover": task.get("videoCover"),
                    "tags": task.get("tags"),
                    "content": text,
                    "startTime": round(float(segment.start), 2),
                    "endTime": round(float(segment.end), 2),
                    "contentVector": vector,
                    "embeddingModel": EMBEDDING_MODEL,
                    "createTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                },
            }
        )
    return docs


def index_docs(es_client, docs):
    if not docs:
        return 0
    success_count, errors = helpers.bulk(es_client, docs, raise_on_error=False)
    if errors:
        raise RuntimeError(f"写入 ES 部分失败: {errors[:3]}")
    return success_count


def delete_source_video(task):
    source_video = Path(task["sourceVideoPath"])
    if source_video.exists():
        source_video.unlink()
        logger.info("已删除字幕处理源文件, path=%s", source_video)


def handle_task(model, es_client, redis_client, task_json):
    task = json.loads(task_json)
    mark_task_processing(redis_client, task)
    start = time.time()
    docs = transcribe_and_embed(model, task)
    success_count = index_docs(es_client, docs)

    # 到这里说明 ES 写入已经成功，保留的 temp.mp4 可以释放掉。
    delete_source_video(task)
    logger.info(
        "字幕向量化完成, videoId=%s, fileId=%s, segments=%s, cost=%.2fs",
        task.get("videoId"),
        task.get("fileId"),
        success_count,
        time.time() - start,
    )
    mark_task_success(redis_client, task)


def main():
    patch_nvidia_libs()
    from faster_whisper import WhisperModel

    redis_client = build_redis_client()
    es_client = build_es_client()

    model = load_whisper_model(WhisperModel, WHISPER_DEVICE, WHISPER_COMPUTE_TYPE)
    current_device = WHISPER_DEVICE
    logger.info(
        "AI Worker 启动完成, queue=%s, deadQueue=%s, esIndex=%s, maxRetry=%s",
        QUEUE_KEY,
        DEAD_QUEUE_KEY,
        ES_INDEX,
        MAX_RETRY_COUNT,
    )

    while True:
        item = redis_client.brpop(QUEUE_KEY, timeout=REDIS_BLOCK_SECONDS)
        if not item:
            continue
        _queue, task_json = item
        try:
            handle_task(model, es_client, redis_client, task_json)
        except RuntimeError as error:
            if current_device.lower() != "cpu" and is_cuda_runtime_error(error):
                logger.warning(
                    "CUDA 运行库不可用，当前任务改用 CPU 重试；如需继续使用 GPU，请配置 EASYLIVE_NVIDIA_DLL_DIRS 或安装 CUDA DLL 到当前 venv"
                )
                model = load_whisper_model(WhisperModel, "cpu", "int8")
                current_device = "cpu"
                try:
                    handle_task(model, es_client, redis_client, task_json)
                except Exception as retry_error:
                    logger.exception("CPU 重试后字幕向量化仍失败, task=%s", task_json)
                    requeue_or_dead_letter(redis_client, task_json, retry_error)
                continue
            logger.exception("字幕向量化任务失败, task=%s", task_json)
            requeue_or_dead_letter(redis_client, task_json, error)
        except Exception as error:
            logger.exception("字幕向量化任务失败, task=%s", task_json)
            requeue_or_dead_letter(redis_client, task_json, error)


if __name__ == "__main__":
    main()
