# Easylive AI Worker

Python Worker 负责消费 Java 投递的字幕向量化任务：

1. 从 Redis 队列 `easylive:queue:ai:subtitle-vector` 读取任务。
2. 用 `faster-whisper` 提取视频字幕。
3. 用本地 Ollama `bge-m3-cpu:567m` 生成字幕片段向量。
4. 批量写入 Elasticsearch 索引 `easylive_video_subtitle_vector`。
5. 写入成功后删除 Java 为字幕处理保留的 `temp.mp4`。

## 启动

```bash
cd ai-worker
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python worker.py
```

如果一台新机器没有装过 CUDA 运行库，但又要使用 GPU，可以额外安装：

```bash
pip install -r requirements-gpu.txt
```

## 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `EASYLIVE_AI_QUEUE` | `easylive:queue:ai:subtitle-vector` | Redis 任务队列 |
| `EASYLIVE_REDIS_URL` | `redis://127.0.0.1:6379/0` | Redis 地址 |
| `EASYLIVE_ES_URL` | `http://127.0.0.1:9201` | Elasticsearch 地址 |
| `EASYLIVE_ES_INDEX` | `easylive_video_subtitle_vector` | 字幕向量索引 |
| `EASYLIVE_EMBEDDING_MODEL` | `bge-m3-cpu:567m` | Ollama 向量模型，默认走 CPU，避免抢占显存 |
| `EASYLIVE_WHISPER_MODEL` | `small` | Whisper 模型 |
| `EASYLIVE_WHISPER_DEVICE` | `cuda` | `cuda` 或 `cpu` |
| `EASYLIVE_WHISPER_COMPUTE_TYPE` | `float16` | GPU 常用 `float16`，CPU 可改 `int8` |
| `EASYLIVE_NVIDIA_DLL_DIRS` | 空 | Windows 下额外补充 NVIDIA DLL 目录，多个目录用分号分隔 |

## Windows CUDA 说明

如果启动后看到 `cublas64_12.dll is not found or cannot be loaded`，说明当前 `.venv` 这个 Python 进程找不到 CUDA 运行库。你本机其他 Python 环境装过也没关系，可以直接把 DLL 所在目录加给 Worker：

```powershell
$env:EASYLIVE_NVIDIA_DLL_DIRS="D:\path\to\nvidia\cublas\bin;D:\path\to\nvidia\cudnn\bin"
python worker.py
```

如果暂时不想处理 GPU 环境，可以先用 CPU 跑通链路：

```powershell
$env:EASYLIVE_WHISPER_DEVICE="cpu"
$env:EASYLIVE_WHISPER_COMPUTE_TYPE="int8"
python worker.py
```

Worker 也做了兜底：默认按 `cuda + float16` 启动，遇到 CUDA DLL 加载失败时，会把当前任务切到 `cpu + int8` 重试一次。

补充说明：Worker 会自动扫描当前 `.venv` 和创建这个 `.venv` 的基础 Python 目录，比如 `D:\pycharm file\Lib\site-packages\nvidia`。如果 CUDA 包就是装在这个基础 Python 里，通常不需要额外安装 `requirements-gpu.txt`，也不需要额外配置 `EASYLIVE_NVIDIA_DLL_DIRS`。

## Ollama 向量模型走 CPU

本项目默认把 `bge-m3` 做成 CPU 专用别名，把 4GB 显存优先留给 `qwen2.5:3b`：

```powershell
ollama create bge-m3-cpu:567m -f ..\ollama\Modelfile.bge-m3-cpu
ollama run bge-m3-cpu:567m "warmup"
ollama ps
```

`ollama ps` 中 `bge-m3-cpu:567m` 的 `PROCESSOR` 应该显示为 CPU。

如果加载 `qwen2.5:3b` 后 `bge-m3-cpu:567m` 仍被卸载，说明 Ollama 服务进程的 `OLLAMA_MAX_LOADED_MODELS` 还是 1。把 `OLLAMA_MAX_LOADED_MODELS=2` 配到启动 Ollama 的环境里，再重启 Ollama。
