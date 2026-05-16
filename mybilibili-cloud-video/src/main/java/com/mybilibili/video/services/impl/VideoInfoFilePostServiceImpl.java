package com.mybilibili.video.services.impl;

import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UploadingFileDTO;
import com.mybilibili.base.entity.event.VideoTransferEvent;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.po.VideoInfoFile;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.entity.po.VideoInfoPost;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.video.entity.query.VideoInfoFilePostQuery;
import com.mybilibili.video.entity.query.VideoInfoFileQuery;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.VideoFileTransferResultEnum;
import com.mybilibili.base.enums.VideoStatusEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.video.mappers.VideoInfoFileMapper;
import com.mybilibili.video.mappers.VideoInfoFilePostMapper;
import com.mybilibili.video.mappers.VideoInfoMapper;
import com.mybilibili.video.mappers.VideoInfoPostMapper;
import com.mybilibili.video.consumer.AiSubtitleVectorClient;
import com.mybilibili.video.consumer.SearchVideoClient;
import com.mybilibili.video.services.VideoInfoFilePostService;
import com.mybilibili.common.utils.FFmpegUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author amani
 * @date 2026/02/09
 * @description 视频文件信息Service
 */

@Slf4j
@Service("VideoInfoFilePostService")
public class VideoInfoFilePostServiceImpl implements VideoInfoFilePostService {

	private static ExecutorService executorService = Executors.newFixedThreadPool(10);
	@Resource
	private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;
	@Resource
	private VideoRedisComponent videoRedisComponent;
	@Resource
	private AdminConfig adminConfig;
	@Resource
	private VideoInfoPostMapper videoInfoPostMapper;
	@Resource
	private FFmpegUtils fFmpegUtils;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Resource
    private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;
	@Resource
	private SearchVideoClient searchVideoClient;
	@Resource
	private AiSubtitleVectorClient aiSubtitleVectorClient;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<VideoInfoFilePost> findListByParam(VideoInfoFilePostQuery param) {
		return this.videoInfoFilePostMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoInfoFilePostQuery param) {
		return this.videoInfoFilePostMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<VideoInfoFilePost> findListByPage(VideoInfoFilePostQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfoFilePost> list = this.findListByParam(param);
		PaginationResultVO<VideoInfoFilePost> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(VideoInfoFilePost bean) {
		return this.videoInfoFilePostMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfoFilePost>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoFilePostMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfoFilePost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoFilePostMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 FileId查询
	 */
	@Override
	public VideoInfoFilePost getVideoInfoFilePostByFileId(String fileId) {
		return this.videoInfoFilePostMapper.selectByFileId(fileId);
	}

	/**
	 * @description 根据 FileId更新
	 */
	@Override
	public Integer updateVideoInfoFilePostByFileId(VideoInfoFilePost bean, String fileId) {
		return this.videoInfoFilePostMapper.updateByFileId(bean, fileId);
	}

	/**
	 * @description 根据 FileId删除
	 */
	@Override
	public Integer deleteVideoInfoFilePostByFileId(String fileId) {
		return this.videoInfoFilePostMapper.deleteByFileId(fileId);
	}


	/**
	 * @description 根据 UploadIdAndUserId查询
	 */
	@Override
	public VideoInfoFilePost getVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId) {
		return this.videoInfoFilePostMapper.selectByUploadIdAndUserId(uploadId, userId);
	}

	/**
	 * @description 根据 UploadIdAndUserId更新
	 */
	@Override
	public Integer updateVideoInfoFilePostByUploadIdAndUserId(VideoInfoFilePost bean, String uploadId, String userId) {
		return this.videoInfoFilePostMapper.updateByUploadIdAndUserId(bean, uploadId, userId);
	}

	/**
	 * @description 根据 UploadIdAndUserId删除
	 */
	@Override
	public Integer deleteVideoInfoFilePostByUploadIdAndUserId(String uploadId, String userId) {
		return this.videoInfoFilePostMapper.deleteByUploadIdAndUserId(uploadId, userId);
	}

	@Override
    public void transferVideo(VideoTransferEvent transferEvent){
        if (transferEvent == null || transferEvent.getFileId() == null) {
            return;
        }
        VideoInfoFilePost transferVideo = videoInfoFilePostMapper.selectByFileId(transferEvent.getFileId());
        if (transferVideo == null && transferEvent.getUploadId() != null && transferEvent.getUserId() != null) {
            // 兼容 fileId 缺失或历史脏消息，按 uploadId + userId 再兜底查一次。
            transferVideo = videoInfoFilePostMapper.selectByUploadIdAndUserId(transferEvent.getUploadId(),
                    transferEvent.getUserId());
        }
        if (transferVideo == null) {
            log.warn("转码任务对应的分 P 记录不存在, event={}", transferEvent);
            return;
        }
        // 已经不是转码中的任务时直接跳过，避免 MQ 重投导致重复执行 ffmpeg。
        if (!VideoFileTransferResultEnum.TRANSFER.getStatus().equals(transferVideo.getTransferResult())) {
            log.info("转码任务已处理或状态已变化, fileId={}, transferResult={}",
                    transferVideo.getFileId(), transferVideo.getTransferResult());
            return;
        }
        VideoInfoFilePost updateFilePost = new VideoInfoFilePost();
        try {
            String key = Constants.REDIS_WEB_UPLOADING_FILE_INFO_KEY + transferVideo.getUserId() + transferVideo.getUploadId();
            UploadingFileDTO uploadFileInfo = videoRedisComponent.getUploadFileInfo(key);
            if (uploadFileInfo == null) {
                log.warn("上传文件元数据不存在, fileId={}, uploadId={}, userId={}",
                        transferVideo.getFileId(), transferVideo.getUploadId(), transferVideo.getUserId());
                updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAILED.getStatus());
                return;
            }
            String tempFilePath = adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + Constants.FILE_PATH_FOLDER_TEMP + uploadFileInfo.getFilePath();
            String targetFilePath = adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + Constants.FILE_PATH_FOLDER_VIDEO + uploadFileInfo.getFilePath();
            File targetFile = new File(targetFilePath);
			File tempFile = new File(tempFilePath);

			FileUtils.copyDirectory(tempFile, targetFile);
			//删除临时目录
			FileUtils.forceDelete(tempFile);
			videoRedisComponent.delUploadVideoInfo(transferVideo.getUserId(), transferVideo.getUploadId());

			String completeFilePath = targetFilePath + Constants.FILE_TEMP_MP4;
			//合并文件
			union(targetFilePath, completeFilePath, true);
			//设置合成文件路径,大小信息
			Integer videoInfoDuration = fFmpegUtils.getVideoInfoDuration(completeFilePath);
			updateFilePost.setDuration(videoInfoDuration);
			updateFilePost.setFileSize(new File(completeFilePath).length());
			updateFilePost.setFilePath(Constants.FILE_PATH_FOLDER_VIDEO + uploadFileInfo.getFilePath());
			updateFilePost.setTransferResult(VideoFileTransferResultEnum.SUCCESS.getStatus());
			// 生成Ts
			convert2Ts(completeFilePath);
		}catch (Exception e) {
            // 转码失败更新状态，后续由投稿主表汇总判断整稿状态。
            updateFilePost.setTransferResult(VideoFileTransferResultEnum.FAILED.getStatus());
            log.error("文件转码失败", e);
        }finally {
            videoInfoFilePostMapper.updateByUploadIdAndUserId(updateFilePost, transferVideo.getUploadId(), transferVideo.getUserId());
            //检查是否全部转码完毕,设置文件持续时间总和
            VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
            videoInfoFilePostQuery.setVideoId(transferVideo.getVideoId());
            videoInfoFilePostQuery.setUserId(transferVideo.getUserId());
            videoInfoFilePostQuery.setTransferResult(VideoFileTransferResultEnum.FAILED.getStatus());
            Integer isFailedCounts = this.findCountByParam(videoInfoFilePostQuery);
            //如果有没有转换完成的,设置转换失败
            if (isFailedCounts > 0)
            {
                VideoInfoPost updateInfoPost = new VideoInfoPost();
                updateInfoPost.setStatus(VideoStatusEnum.STATUS_1.getStatus());
                videoInfoPostMapper.updateByVideoId(updateInfoPost,transferVideo.getVideoId());
                return;
            }
            videoInfoFilePostQuery.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
            Integer transferCount = this.findCountByParam(videoInfoFilePostQuery);
            //转码成功
            if (transferCount == 0)
            {
                //设置待审核状态
                Integer duration = videoInfoFilePostMapper.getSumDuration(transferVideo.getVideoId());
				VideoInfoPost videoInfoPost = new VideoInfoPost();
				videoInfoPost.setDuration(duration);
				videoInfoPost.setStatus(VideoStatusEnum.STATUS_2.getStatus());
				videoInfoPostMapper.updateByVideoId(videoInfoPost,transferVideo.getVideoId());
			}
		}
	}
	private void convert2Ts(String completeFilePath)
	{
		File videoFile = new File(completeFilePath);
		File tsFolder = videoFile.getParentFile();
		String videoCodec = fFmpegUtils.getVideoCodec(completeFilePath);
		if (Constants.VIDEO_CODEC_HEVC.equals(videoCodec))
		{
			String tempFileName = completeFilePath + Constants.FILE_VIDEO_TEMP_SUFFIX;
			new File(completeFilePath).renameTo(new File(tempFileName));
			fFmpegUtils.convertHevc2Mp4(tempFileName, completeFilePath);
			new File(tempFileName).delete();
		}
		fFmpegUtils.convertVideo2Ts(tsFolder, completeFilePath);
		// temp.mp4 先保留下来，审核通过后的 Python Worker 要用它提取字幕。
		// Worker 成功写入字幕向量后再删除，避免审核后找不到源视频。
	}

	private void union(String dirPath, String toFilePath, Boolean isDelSource)
	{
		File dir = new File(dirPath);
		if (!dir.exists())
			throw new BusinessException("目录不存在");
		File fileList[] = dir.listFiles();
		File targetFile = new File(toFilePath);
		try (RandomAccessFile wf = new RandomAccessFile(targetFile, "rw")){
			byte[] b = new byte[1024 * 10];
			for (int i = 0; i < fileList.length; i ++)
			{
				int len = -1;
				File chunckFile = new File(dirPath  + File.separator + i);
				RandomAccessFile readFile = null;
				try {
					readFile = new RandomAccessFile(chunckFile, "r");
					while ((len = readFile.read(b) )!= -1)
					{
						wf.write(b,0, len);
					}
				}catch (Exception e)
				{
					log.error("合并分片失败", e);
					throw new BusinessException("合并文件失败");
				}finally {
					readFile.close();
				}
			}
		}catch (Exception e)
		{
			throw new BusinessException("合并文件" + dirPath + "出错了");
		}finally {
			if (isDelSource)
			{
				for (int i = 0;i < fileList.length; i ++)
				{
					fileList[i].delete();
				}
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
    public void deleVideo(String videoId, String userId, Boolean isAdmin) {
		VideoInfoPost videoInfo = (VideoInfoPost)videoInfoPostMapper.selectByVideoId(videoId);

		if (videoInfo == null || !isAdmin && !videoInfo.getUserId().equals(userId))
			throw new BusinessException(ResponseCodeEnum.CODE_404);

		videoInfoMapper.deleteByVideoId(videoId);
		videoInfoPostMapper.deleteByVideoId(videoId);
		deleteAiSubtitleVectorSilently(videoId);

		// 2. 注册一个事务同步回调：只有当事务成功提交（COMMIT）后，才触发异步逻辑
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				deleteSearchDocSilently(videoId);
				executorService.submit(()-> {
					VideoInfoFileQuery fileQuery = new VideoInfoFileQuery();
					if (!isAdmin)
						fileQuery.setUserId(userId);
					fileQuery.setVideoId(videoId);
					videoInfoFileMapper.deleteByCondition(fileQuery);
					VideoInfoFilePostQuery videoInfoFilePostQuery = new VideoInfoFilePostQuery();
					videoInfoFilePostQuery.setVideoId(videoId);
					videoInfoFilePostQuery.setUserId(userId);
					List<VideoInfoFilePost> postList = videoInfoFilePostMapper.selectList(videoInfoFilePostQuery);
					videoInfoFilePostMapper.deleteByCondition(videoInfoFilePostQuery);
					//删除文件
					postList.forEach(item ->{
						String filePath = adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + item.getFilePath();
						try {
							FileUtils.deleteDirectory(new File(filePath));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
					log.error("删除完成");
				});
			}
		});

	}

	private void deleteSearchDocSilently(String videoId) {
		try {
			searchVideoClient.deleteVideoDoc(videoId);
		} catch (Exception e) {
			// MySQL 删除已提交，搜索索引失败只能记录日志，后续用重建索引兜底。
			log.error("删除视频搜索索引失败, videoId={}", videoId, e);
		}
	}

	private void deleteAiSubtitleVectorSilently(String videoId) {
		try {
			aiSubtitleVectorClient.deleteByVideoId(videoId);
		} catch (Exception e) {
			// 删除视频的主链路是 MySQL 和文件清理，字幕向量删除失败只记日志，后续可重跑清理。
			log.error("删除视频字幕向量失败, videoId={}", videoId, e);
		}
	}
}
