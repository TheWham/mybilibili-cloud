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
REDIS_URL = os.getenv("EASYLIVE_REDIS_URL", "redis://127.0.0.1:6379/0")
ES_URL = os.getenv("EASYLIVE_ES_URL", "http://127.0.0.1:9201")
ES_INDEX = os.getenv("EASYLIVE_ES_INDEX", "easylive_video_subtitle_vector")
# 默认使用 CPU 专用的 embedding 模型别名。
# 这样 Worker 做字幕向量化时不会抢占显存，避免影响 Web 侧 qwen2.5:3b 的响应速度。
EMBEDDING_MODEL = os.getenv("EASYLIVE_EMBEDDING_MODEL", "bge-m3-cpu:567m")
WHISPER_MODEL = os.getenv("EASYLIVE_WHISPER_MODEL", "small")
WHISPER_DEVICE = os.getenv("EASYLIVE_WHISPER_DEVICE", "cuda")
WHISPER_COMPUTE_TYPE = os.getenv("EASYLIVE_WHISPER_COMPUTE_TYPE", "float16")
REDIS_BLOCK_SECONDS = int(os.getenv("EASYLIVE_REDIS_BLOCK_SECONDS", "5"))
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


def handle_task(model, es_client, task_json):
    task = json.loads(task_json)
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


def main():
    patch_nvidia_libs()
    from faster_whisper import WhisperModel

    redis_client = build_redis_client()
    es_client = build_es_client()

    model = load_whisper_model(WhisperModel, WHISPER_DEVICE, WHISPER_COMPUTE_TYPE)
    current_device = WHISPER_DEVICE
    logger.info("AI Worker 启动完成, queue=%s, esIndex=%s", QUEUE_KEY, ES_INDEX)

    while True:
        item = redis_client.brpop(QUEUE_KEY, timeout=REDIS_BLOCK_SECONDS)
        if not item:
            continue
        _queue, task_json = item
        try:
            handle_task(model, es_client, task_json)
        except RuntimeError as error:
            if current_device.lower() != "cpu" and is_cuda_runtime_error(error):
                logger.warning(
                    "CUDA 运行库不可用，当前任务改用 CPU 重试；如需继续使用 GPU，请配置 EASYLIVE_NVIDIA_DLL_DIRS 或安装 CUDA DLL 到当前 venv"
                )
                model = load_whisper_model(WhisperModel, "cpu", "int8")
                current_device = "cpu"
                try:
                    handle_task(model, es_client, task_json)
                except Exception:
                    logger.exception("CPU 重试后字幕向量化仍失败, task=%s", task_json)
                continue
            logger.exception("字幕向量化任务失败, task=%s", task_json)
        except Exception:
            # 第一版先不做死信队列，避免坏任务阻塞队列；日志里保留完整任务方便人工重投。
            logger.exception("字幕向量化任务失败, task=%s", task_json)


if __name__ == "__main__":
    main()
