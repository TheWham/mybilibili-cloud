package com.mybilibili.video.services.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.AiSubtitleIndexTaskDTO;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.dto.VideoCountDTO;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.video.consumer.SearchVideoClient;
import com.mybilibili.video.consumer.UserVideoActionClient;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.po.VideoInfoFile;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.entity.po.VideoInfoPost;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.base.enums.VideoFileUpdateTypeEnum;
import com.mybilibili.base.enums.VideoRecommendEnum;
import com.mybilibili.base.enums.VideoStatusEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.entity.dto.VideoCountUpdateDTO;
import com.mybilibili.video.entity.query.VideoInfoFilePostQuery;
import com.mybilibili.video.entity.query.VideoInfoFileQuery;
import com.mybilibili.video.entity.query.VideoInfoPostQuery;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.entity.vo.VideoInfoResultVO;
import com.mybilibili.video.mappers.VideoInfoFileMapper;
import com.mybilibili.video.mappers.VideoInfoFilePostMapper;
import com.mybilibili.video.mappers.VideoInfoMapper;
import com.mybilibili.video.mappers.VideoInfoPostMapper;
import com.mybilibili.video.consumer.AiSubtitleVectorClient;
import com.mybilibili.video.mq.producer.UserMessageEventProducer;
import com.mybilibili.video.services.VideoInfoService;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author amani
 * @date 2026/02/09
 * @description 视频信息Service
 */

@Service("VideoInfoService")
public class VideoInfoServiceImpl implements VideoInfoService {
	private static final Logger log = LoggerFactory.getLogger(VideoInfoServiceImpl.class);
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private VideoInfoFileMapper<VideoInfoFile, VideoInfoFileQuery> videoInfoFileMapper;
	@Resource
	private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

	@Resource
	private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;
	@Resource
	private VideoRedisComponent videoRedisComponent;
	@Resource
	private AdminConfig adminConfig;
	@Resource
	private SearchVideoClient searchVideoClient;
	@Resource
	private AiSubtitleVectorClient aiSubtitleVectorClient;

	@Resource
	private UserVideoActionClient userVideoActionClient;
	@Resource
	private UserMessageEventProducer userMessageEventProducer;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<VideoInfo> findListByParam(VideoInfoQuery param) {
		List<VideoInfo> list = this.videoInfoMapper.selectList(param);
		mergeRedisActionDelta(list);
		return list;
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoInfoQuery param) {
		return this.videoInfoMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<VideoInfo> findListByPage(VideoInfoQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfo> list = this.findListByParam(param);
		PaginationResultVO<VideoInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(VideoInfo bean) {
		return this.videoInfoMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfo>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * @description 单例新增/修改
	 */
	@Override
	public Integer addOrUpdate(VideoInfo videoBean) {
		if (videoBean == null) {
			return 0;
		}
		return this.videoInfoMapper.insertOrUpdate(videoBean);
	}

	/**
	 * 管理员审核视频投稿。
	 *
	 * <p>这里把事务边界收在 Service 层，Controller 只负责接参。审核主链路只处理
	 * MySQL 中的业务事实；依赖提交结果的通知、索引、AI 任务、文件清理统一挂到
	 * afterCommit，避免出现数据库回滚但副作用先执行的问题。</p>
	 *
	 * @param videoId 视频 ID
	 * @param status  审核状态，只允许审核通过或审核驳回
	 * @param reason  驳回原因，审核通过时允许为空
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void auditVideo(String videoId, Integer status, String reason) {
		//校验参数
		VideoInfoPost videoInfoPost = this.videoInfoPostMapper.selectByVideoId(videoId);
		if (videoInfoPost == null ||(!status.equals(VideoStatusEnum.STATUS_3.getStatus()) && !status.equals(VideoStatusEnum.STATUS_4.getStatus()))){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		VideoInfoPostQuery postQuery = new VideoInfoPostQuery();
		postQuery.setVideoId(videoId);
		postQuery.setStatus(VideoStatusEnum.STATUS_2.getStatus());
		VideoInfoPost updateInfoPost = new VideoInfoPost();
		updateInfoPost.setStatus(status);

		//update info
		Integer updateCount = videoInfoPostMapper.updateByCondition(updateInfoPost, postQuery);
		if (updateCount == 0)
			throw new BusinessException("正式表更新失败");

		if (status.equals(VideoStatusEnum.STATUS_4.getStatus())) {
			registerAfterCommit(() -> userMessageEventProducer.sendAuditVideoMessage(videoInfoPost, status, reason));
			return;
		}

		//审核完成修改更新方式为未更新
		VideoInfoFilePost videoInfoFilePost = new VideoInfoFilePost();
		videoInfoFilePost.setUpdateType(VideoFileUpdateTypeEnum.UN_UPDATE.getStatus());
		VideoInfoFilePostQuery filePostQuery = new VideoInfoFilePostQuery();
		filePostQuery.setVideoId(videoId);
		videoInfoFilePostMapper.updateByCondition(videoInfoFilePost, filePostQuery);

		//同步信息到正式表
		VideoInfo videoInfo = this.videoInfoMapper.selectByVideoId(videoId);
		boolean firstAuditPass = videoInfo == null;

		videoInfo = BeanUtil.toBean(videoInfoPost, VideoInfo.class);
		this.addOrUpdate(videoInfo);

		//不是第一次,表示有信息新增或者修改, 需要先清空再添加
		VideoInfoFileQuery videoInfoFileQuery = new VideoInfoFileQuery();
		videoInfoFileQuery.setVideoId(videoId);
		videoInfoFileMapper.deleteByCondition(videoInfoFileQuery);

		//更新videoInfoFile表信息
		List<VideoInfoFilePost> filePostList = videoInfoFilePostMapper.selectList(filePostQuery);
		List<VideoInfoFile> videoInfoFiles = BeanUtil.copyToList(filePostList, VideoInfoFile.class);
		videoInfoFileMapper.insertOrUpdateBatch(videoInfoFiles);


		Integer auditRewardCoinCount = null;
		if (firstAuditPass) {
			SysSettingDTO sysSetting = videoRedisComponent.getSysSetting();
			auditRewardCoinCount = sysSetting.getPostVideoCoinCount();
		}

		// 这里只读取待删目录清单，真正的物理删除放到事务提交之后，避免文件删掉了事务却回滚。
		List<String> deleteFilePathList = videoRedisComponent.getDelFilePathsQueue(videoId);
		videoRedisComponent.cleanDelFilePaths(videoId);
		VideoInfo auditedVideoInfo = videoInfo;
		List<VideoInfoFilePost> auditedFilePostList = filePostList;
		Integer auditedRewardCoinCount = auditRewardCoinCount;
		registerAfterCommit(() -> handleAuditPassAfterCommit(auditedVideoInfo, auditedFilePostList,
				videoInfoPost, status, reason, auditedRewardCoinCount, deleteFilePathList));
	}

	@Override
	public Integer reportVideoPlayOnline(String fileId, String deviceId) {
		Integer count = videoRedisComponent.reportVideoPlayOnline(fileId, deviceId);
		return count;
	}

	@Override
	public VideoCountDTO sumVideoCountByUserId(String userId) {
		return this.videoInfoMapper.sumVideoCountByUserId(userId);
	}

	@Override
	public List<VideoInfo> selectByIds(List<String> userCollectionIds) {
		List<VideoInfo> videoInfoList = videoInfoMapper.selectByIds(userCollectionIds);
		mergeRedisActionDelta(videoInfoList);
		return videoInfoList;
	}

	@Override
	public List<VideoInfo> selectVideoListBySeriesIdAndUserId(Integer seriesId, String userId) {
		List<VideoInfo> videoInfoList = videoInfoMapper.selectVideoListBySeriesIdAndUserId(seriesId, userId);
		mergeRedisActionDelta(videoInfoList);
		return videoInfoList;
	}

	@Override
	public Integer updateCountBatch(String field, List<VideoCountUpdateDTO> list) {
		Integer count = this.videoInfoMapper.updateCountBatch(field, list);
		return count;
	}

	@Override
	public void recommendVideo(String videoId) {
		VideoInfo videoInfo = this.videoInfoMapper.selectByVideoId(videoId);
		VideoInfoPost videoInfoPost = this.videoInfoPostMapper.selectByVideoId(videoId);

		Optional.ofNullable(videoInfo).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

		if (!(videoInfoPost.getStatus().equals(VideoStatusEnum.STATUS_3.getStatus())))
			throw new BusinessException(ResponseCodeEnum.CODE_600);

		VideoInfo updateInfo = new VideoInfo();

		if (videoInfo.getRecommendType().equals(VideoRecommendEnum.NO_RECOMMEND.getStatus()))
			updateInfo.setRecommendType(VideoRecommendEnum.RECOMMEND.getStatus());

		if (videoInfo.getRecommendType().equals(VideoRecommendEnum.RECOMMEND.getStatus()))
			updateInfo.setRecommendType(VideoRecommendEnum.NO_RECOMMEND.getStatus());

		this.videoInfoMapper.updateByVideoId(updateInfo, videoId);
	}

	@Override
	public VideoInfoResultVO getVideoInfoResultVO(String videoId, String userId) {

		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);
		if (videoInfo == null)
			throw new BusinessException(ResponseCodeEnum.CODE_404);
		VideoInfoResultVO videoInfoResultVO = new VideoInfoResultVO(videoInfo);
		//查询是否投币,点赞,收藏
		UserActionQuery actionQuery = new UserActionQuery();
		actionQuery.setUserId(userId);
		actionQuery.setVideoId(videoId);
		actionQuery.setUserActionTypeList(new Integer[]{UserActionTypeEnum.VIDEO_LIKE.getType(), UserActionTypeEnum.VIDEO_COIN.getType(), UserActionTypeEnum.VIDEO_COLLECT.getType()});
		//调用interact feign
		List<UserActionVO> userActionTypeList = userVideoActionClient.getUserActionTypeList(actionQuery);
		videoInfoResultVO.setUserActionList(userActionTypeList);
		return videoInfoResultVO;
	}

	/**
	 * @description 根据 VideoId查询
	 */
	@Override
	public VideoInfo getVideoInfoByVideoId(String videoId) {
		VideoInfo videoInfo = this.videoInfoMapper.selectByVideoId(videoId);
		if (videoInfo == null) {
			return null;
		}
		log.info(
				"getVideoInfoByVideoId db snapshot, videoId={}, likeCount={}, collectCount={}, coinCount={}",
				videoId,
				defaultValue(videoInfo.getLikeCount()),
				defaultValue(videoInfo.getCollectCount()),
				defaultValue(videoInfo.getCoinCount())
		);
		mergeRedisActionDelta(videoInfo);
		log.info(
				"getVideoInfoByVideoId merged snapshot, videoId={}, likeCount={}, collectCount={}, coinCount={}",
				videoId,
				defaultValue(videoInfo.getLikeCount()),
				defaultValue(videoInfo.getCollectCount()),
				defaultValue(videoInfo.getCoinCount())
		);
		return videoInfo;
	}

	/**
	 * @description 根据 VideoId更新
	 */
	@Override
	public Integer updateVideoInfoByVideoId(VideoInfo bean, String videoId) {
		return this.videoInfoMapper.updateByVideoId(bean, videoId);
	}

	/**
	 * @description 根据 VideoId删除
	 */
	@Override
	public Integer deleteVideoInfoByVideoId(String videoId) {
		return this.videoInfoMapper.deleteByVideoId(videoId);
	}

	@Override
	public Integer updateByCondition(VideoInfo videoInfo, VideoInfoQuery videoInfoQuery) {
		return this.videoInfoMapper.updateByCondition(videoInfo, videoInfoQuery);
	}

	private void mergeRedisActionDelta(VideoInfo videoInfo) {
		if (videoInfo == null) {
			return;
		}
		Map<String, Integer> deltaMap = videoRedisComponent.getVideoActionCountDelta(videoInfo.getVideoId());
		if (deltaMap == null || deltaMap.isEmpty()) {
			log.info("getVideoInfoByVideoId redis delta empty, videoId={}", videoInfo.getVideoId());
			return;
		}
		log.info(
				"getVideoInfoByVideoId redis delta, videoId={}, likeDelta={}, collectDelta={}, coinDelta={}",
				videoInfo.getVideoId(),
				deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_LIKE.getField(), 0),
				deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COLLECT.getField(), 0),
				deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COIN.getField(), 0)
		);

		// 视频详情页要求用户操作后刷新立刻能看到结果。
		// MySQL 里的计数是异步同步的，所以这里把 Redis 中尚未落库的增量补到返回值上。
		videoInfo.setLikeCount(nonNegative(defaultValue(videoInfo.getLikeCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_LIKE.getField(), 0)));
		videoInfo.setCollectCount(nonNegative(defaultValue(videoInfo.getCollectCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COLLECT.getField(), 0)));
		videoInfo.setCoinCount(nonNegative(defaultValue(videoInfo.getCoinCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COIN.getField(), 0)));
	}

	private void mergeRedisActionDelta(List<VideoInfo> videoInfoList) {
		if (videoInfoList == null || videoInfoList.isEmpty()) {
			return;
		}
		// 前台列表、推荐列表和收藏列表之前都是直接查 MySQL。
		// 详情页已经补了 Redis 增量，如果列表页不补，就会出现“详情页是新值，列表还是旧值”的割裂感。
		for (VideoInfo videoInfo : videoInfoList) {
			mergeRedisActionDelta(videoInfo);
		}
	}

	private int defaultValue(Integer value) {
		return value == null ? 0 : value;
	}

	private int nonNegative(int value) {
		return Math.max(value, 0);
	}

	/**
	 * 审核通过后的提交后处理。
	 *
	 * <p>这里只有“提交之后才能做”的事情：发奖励、同步搜索索引、投递 AI 字幕任务、
	 * 发送站内信以及清理旧文件。任何一个分支失败都只记日志，不反向影响审核结果。</p>
	 */
	private void handleAuditPassAfterCommit(VideoInfo videoInfo,
											List<VideoInfoFilePost> filePostList,
											VideoInfoPost videoInfoPost,
											Integer status,
											String reason,
											Integer rewardCoinCount,
											List<String> deleteFilePathList) {
		addVideoAuditRewardSilently(videoInfoPost.getUserId(), videoInfoPost.getVideoId(), rewardCoinCount);
		saveVideoDocSilently(videoInfo);
		enqueueAiSubtitleIndexTasks(videoInfo, filePostList);
		deleteAuditRemovedFilesSilently(deleteFilePathList);
		userMessageEventProducer.sendAuditVideoMessage(videoInfoPost, status, reason);
	}

	/**
	 * 删除审核通过前被用户替换掉的旧目录。
	 *
	 * <p>这里做的是磁盘 IO，不应该占着数据库事务。等事务提交成功后再删，即使删除失败，
	 * 也只是留下可人工清理的旧文件，不会破坏正式数据。</p>
	 *
	 * @param deleteFilePathList Redis 中记录的待删目录列表
	 */
	private void deleteAuditRemovedFilesSilently(List<String> deleteFilePathList) {
		if (deleteFilePathList == null || deleteFilePathList.isEmpty()) {
			return;
		}
		for (String filePath : deleteFilePathList) {
			String completeFilePath = adminConfig.getProjectFolder() + Constants.FILE_PATH_FOLDER + filePath;
			File file = new File(completeFilePath);
			if (!file.exists()) {
				continue;
			}
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				log.error("删除审核遗留文件失败, filePath={}", completeFilePath, e);
			}
		}
	}

	private void addVideoAuditRewardSilently(String userId, String videoId, Integer rewardCoinCount) {
		if (rewardCoinCount == null || rewardCoinCount <= 0) {
			return;
		}
		try {
			// 首次审核通过才发发布奖励，等事务提交后再补 Redis 和 MQ，避免审核回滚后用户硬币已经变化。
			videoRedisComponent.addVideoAuditReward(userId, videoId, rewardCoinCount);
		} catch (Exception e) {
			log.error("投递视频审核奖励失败, userId={}, videoId={}, rewardCoinCount={}",
					userId, videoId, rewardCoinCount, e);
		}
	}

	private void saveVideoDocSilently(VideoInfo videoInfo) {
		try {
			// 正式表写完后，把搜索索引交给 search 服务维护，video 不再直接操作 ES。
			searchVideoClient.saveVideoDoc(BeanUtil.toBean(videoInfo, VideoInfoDTO.class));
		} catch (Exception e) {
			// 搜索索引是审核后的读侧数据，失败时记录日志，后续可以用重建索引补偿。
			log.error("同步视频搜索索引失败, videoId={}", videoInfo == null ? null : videoInfo.getVideoId(), e);
		}
	}

	private void enqueueAiSubtitleIndexTasks(VideoInfo videoInfo, List<VideoInfoFilePost> filePostList) {
		if (videoInfo == null || filePostList == null || filePostList.isEmpty()) {
			return;
		}
		try {
			deleteAiSubtitleVectorSilently(videoInfo.getVideoId());
			List<AiSubtitleIndexTaskDTO> taskList = new ArrayList<>();
			for (VideoInfoFilePost filePost : filePostList) {
				String sourceVideoPath = adminConfig.getProjectFolder()
						+ Constants.FILE_PATH_FOLDER
						+ filePost.getFilePath()
						+ Constants.FILE_TEMP_MP4;
				File sourceVideo = new File(sourceVideoPath);
				if (!sourceVideo.exists()) {
					log.warn("跳过字幕向量化任务，源视频不存在, videoId={}, fileId={}, path={}",
							videoInfo.getVideoId(), filePost.getFileId(), sourceVideoPath);
					continue;
				}

				AiSubtitleIndexTaskDTO task = new AiSubtitleIndexTaskDTO();
				task.setVideoId(videoInfo.getVideoId());
				task.setUserId(videoInfo.getUserId());
				task.setVideoName(videoInfo.getVideoName());
				task.setVideoCover(videoInfo.getVideoCover());
				task.setTags(videoInfo.getTags());
				task.setFileId(filePost.getFileId());
				task.setFileIndex(filePost.getFileIndex());
				task.setSourceVideoPath(sourceVideoPath);
				taskList.add(task);
			}
			if (taskList.isEmpty()) {
				log.warn("视频字幕向量化任务为空, videoId={}", videoInfo.getVideoId());
				return;
			}

			videoRedisComponent.initAiSubtitleIndexStatus(videoInfo.getVideoId(), taskList.size());
			for (AiSubtitleIndexTaskDTO task : taskList) {
				videoRedisComponent.addAiSubtitleIndexTask(task);
			}
			log.info("视频字幕向量化任务投递完成, videoId={}, count={}", videoInfo.getVideoId(), taskList.size());
		} catch (Exception e) {
			// 字幕向量化是审核后的增强链路，不能因为 AI 队列或 ES 异常影响审核主流程。
			log.error("投递字幕向量化任务失败, videoId={}", videoInfo.getVideoId(), e);
		}
	}

	private void deleteAiSubtitleVectorSilently(String videoId) {
		try {
			aiSubtitleVectorClient.deleteByVideoId(videoId);
		} catch (Exception e) {
			// 删除旧向量失败不能阻断新任务投递，否则 worker 在线也没有机会重建索引。
			log.error("删除旧字幕向量失败, videoId={}", videoId, e);
		}
	}

	/**
	 * 注册事务提交后的回调。
	 *
	 * <p>审核、转码这类链路对提交时机很敏感，所以这里不做“没有事务就直接执行”的降级。
	 * 一旦脱离事务调用，宁可尽早报错，也不能把 afterCommit 语义悄悄改成立即执行。</p>
	 *
	 * @param task 提交成功后需要执行的任务
	 */
	private void registerAfterCommit(Runnable task) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			log.error("afterCommit任务注册失败，当前线程没有可用事务, task={}", task);
			throw new IllegalStateException("当前线程没有可用事务，不能注册 afterCommit 任务");
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				task.run();
			}
		});
	}
}
