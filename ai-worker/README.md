# Easylive AI Worker

Python Worker 负责消费 Java 投递的字幕向量化任务：

1. 从 Redis 队列 `easylive:queue:ai:subtitle-vector` 读取任务。
2. 用 `faster-whisper` 提取视频字幕。
3. 用本地 Ollama `bge-m3:567m` 生成字幕片段向量。
4. 批量写入 Elasticsearch 索引 `easylive_video_subtitle_vector`。
5. 写入成功后删除 Java 为字幕处理保留的 `temp.mp4`。

队列 key 必须和 Java 侧 `Constants.REDIS_AI_SUBTITLE_VECTOR_QUEUE_KEY` 保持一致。
默认值就是 `easylive:queue:ai:subtitle-vector`，如果 Java 配置调整了 Redis 前缀，
这里也要同步改 `EASYLIVE_AI_QUEUE`。

本地基础设施版本和你现有的 `D:\cmd可运行文件\start-all.ps1` 保持一致：

- Nginx: `D:\nginx-projects\easylive-web-nginx\nginx-1.20.2`
- Redis: `D:\redis-windows\Redis-6.2.14-Windows-x64-cygwin`
- Elasticsearch: `D:\elasticSearch\elasticsearch-8.11.4\bin`
- Nacos: `D:\nacos-server-2.4.2.1\nacos`
- Seata: `D:\seata\seata-2.1.0-incubating-bin`
- Ollama: 不进 Docker Compose，继续使用本机安装版

## 启动

```bash
cd ai-worker
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python worker.py
```

Windows 本地开发可以在项目根目录一键启动 `start-all.ps1` 里那套基础设施的 Docker 版本，再启动 worker：

```powershell
.\scripts\start-ai-dev.ps1
```

脚本会连接本机已安装的 Ollama，默认地址仍然是 `http://127.0.0.1:11434`。
默认会按 `Whisper small + cuda + int8_float16` 启动，适合 `RTX 3050 Ti 4GB` 这类显存偏紧的机器。

如果本机已经启动好了这些依赖，只想启动 worker：

```powershell
.\scripts\start-ai-dev.ps1 -SkipInfra
```

如果你已经运行了 `D:\cmd可运行文件\start-all.ps1` 里的任意组件，
`start-ai-dev.ps1` 会直接提示端口冲突。因为这套 compose 默认就是用来替代本机
`Nginx + Redis + Elasticsearch + Nacos + Seata` 的，不会和本机同端口双开。

Nginx 容器使用容器专用配置文件 [nginx.docker.conf](/abs/path/will-be-replaced) 的等价版本，
把前端静态目录映射进容器，并把 `/api/` 代理到宿主机 `7071` 端口上的 gateway。
Nacos 容器复用你本机已有的 `data` 和 `logs` 目录，避免把已经存在的配置中心数据重新初始化。
Seata 容器使用单独的容器版 `application.yml`，把 Nacos 地址从 `127.0.0.1:8848` 改成容器网络里的 `nacos:8848`。

如果一台新机器没有装过 CUDA 运行库，但又要使用 GPU，可以额外安装：

```bash
pip install -r requirements-gpu.txt
```

## 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `EASYLIVE_AI_QUEUE` | `easylive:queue:ai:subtitle-vector` | Redis 任务队列 |
| `EASYLIVE_AI_DEAD_QUEUE` | `easylive:queue:ai:subtitle-vector:dead` | 多次失败后的死信队列 |
| `EASYLIVE_AI_STATUS_PREFIX` | `easylive:ai:subtitle-vector:status:` | 任务状态 Hash 前缀，后面拼 videoId |
| `EASYLIVE_AI_MAX_RETRY_COUNT` | `3` | 单个任务最大重试次数，超过后进入死信队列 |
| `EASYLIVE_REDIS_URL` | `redis://127.0.0.1:6379/0` | Redis 地址 |
| `EASYLIVE_ES_URL` | `http://127.0.0.1:9201` | Elasticsearch 地址 |
| `EASYLIVE_ES_INDEX` | `easylive_video_subtitle_vector` | 字幕向量索引 |
| `EASYLIVE_EMBEDDING_MODEL` | `bge-m3:567m` | Ollama 向量模型，按需加载，是否继续占显存由 Ollama `keep_alive` 决定 |
| `EASYLIVE_WHISPER_MODEL` | `small` | Whisper 模型 |
| `EASYLIVE_WHISPER_DEVICE` | `cuda` | `cuda` 或 `cpu` |
| `EASYLIVE_WHISPER_COMPUTE_TYPE` | `int8_float16` | 4GB 显卡更稳的 GPU 参数；CPU 可改 `int8` |
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

Worker 也做了兜底：默认按 `cuda + int8_float16` 启动，遇到 CUDA DLL 加载失败时，会把当前任务切到 `cpu + int8` 重试一次。

## 3050 Ti 显存建议

你这台机器是 `RTX 3050 Ti 4GB`，默认把 `Whisper` 固定在 `small + cuda + int8_float16` 会更稳。
这个组合比 `float16` 更省显存，通常更适合和 `bge-m3:567m` 错峰使用 GPU。

`bge-m3:567m` 不要求常驻显存。Worker 只有在生成字幕向量时才会调用 Ollama，
模型是否继续留在显存里由 Ollama 的 `keep_alive` 控制；Java 侧当前默认值是 `10m`。

## GPU 使用验证

不要只看任务管理器里的 `3D` 曲线。CUDA 推理很多时候不会明显体现在 `3D` 图表里，
更可靠的观察方式是：

- 查看 NVIDIA 专用显存是否在任务处理时明显上升
- 把任务管理器图表切换到 `CUDA` 或 `Compute_0`
- 使用 `nvidia-smi` 查看当前 Python 和 Ollama 进程的显存占用

如果只是想先把 `Whisper` 跑在 GPU 上，直接执行：

```powershell
.\scripts\start-ai-dev.ps1 -SkipInfra
```

如果想手动覆盖参数，也可以显式指定：

```powershell
.\scripts\start-ai-dev.ps1 -SkipInfra -WhisperDevice cuda -WhisperComputeType int8_float16
```

## 失败重试和状态

Worker 每次取到任务后会把 `easylive:ai:subtitle-vector:status:{videoId}` 更新为处理中。
处理成功后累加 `successCount`，全部分 P 成功后状态变为成功。
如果任务失败，会重新写回主队列；超过 `EASYLIVE_AI_MAX_RETRY_COUNT` 后写入死信队列，
并把状态标记为失败，后台审核列表可以直接看到 AI 索引是否积压或失败。

## Docker volume 说明

这套 compose 使用的是混合存储方式：

- Redis 和 Elasticsearch 继续使用 Docker named volume
- Nacos 复用你本机已有的 `data` / `logs`
- Nginx 直接挂载你本机前端 `dist` 目录

Redis 和 Elasticsearch 的固定 volume 名如下：

- `mybilibili-redis-data`
- `mybilibili-elasticsearch-data`

常用命令：

```powershell
docker volume ls
docker compose -f .\docker-compose.ai-dev.yml down
docker compose -f .\docker-compose.ai-dev.yml down -v
```

`down` 只停容器，保留数据；`down -v` 会把上面两个 volume 一起删除。

补充说明：Worker 会自动扫描当前 `.venv` 和创建这个 `.venv` 的基础 Python 目录，比如 `D:\pycharm file\Lib\site-packages\nvidia`。如果 CUDA 包就是装在这个基础 Python 里，通常不需要额外安装 `requirements-gpu.txt`，也不需要额外配置 `EASYLIVE_NVIDIA_DLL_DIRS`。

## Ollama 向量模型验证

本项目默认直接使用 `bge-m3:567m`。需要确认模型是否已经切到新名字，并且在做向量化时能正常被 Ollama 拉起，可以先执行：

```powershell
ollama run bge-m3:567m "warmup"
ollama ps
```

`ollama ps` 里如果能看到 `bge-m3:567m`，说明模型名已经切过来了。至于 `PROCESSOR` 显示 GPU 还是 CPU，
要看 Ollama 当时的调度结果和本机显存是否充足；如果 `Whisper` 正在跑 GPU，`bge-m3` 也可能回落到 CPU。

如果你希望 `bge-m3` 和其他模型短时间内同时保活，可以把启动 Ollama 的环境变量
`OLLAMA_MAX_LOADED_MODELS` 调大后重启 Ollama；不过在 `4GB` 显卡上，不建议让多个 GPU 模型长期常驻。
