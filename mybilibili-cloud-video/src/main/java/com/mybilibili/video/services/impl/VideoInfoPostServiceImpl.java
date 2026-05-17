package com.mybilibili.video.services.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.event.VideoTransferEvent;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.*;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.base.utils.JsonUtils;
import com.mybilibili.common.component.UserDailyLimitComponent;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.entity.po.VideoInfoPost;
import com.mybilibili.video.entity.query.VideoInfoFilePostQuery;
import com.mybilibili.video.entity.query.VideoInfoPostQuery;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.entity.vo.VideoAuditCountVO;
import com.mybilibili.video.mappers.VideoInfoFilePostMapper;
import com.mybilibili.video.mappers.VideoInfoMapper;
import com.mybilibili.video.mappers.VideoInfoPostMapper;
import com.mybilibili.video.mq.producer.VideoTransferEventProducer;
import com.mybilibili.video.services.VideoInfoPostService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author amani
 * @date 2026/02/13
 * @description 视频信息Service
 */

@Service("VideoInfoPostService")
public class VideoInfoPostServiceImpl implements VideoInfoPostService {
	@Resource
	private VideoInfoPostMapper<VideoInfoPost, VideoInfoPostQuery> videoInfoPostMapper;

	@Resource
	private VideoRedisComponent videoRedisComponent;

	@Resource
	private VideoInfoFilePostMapper<VideoInfoFilePost, VideoInfoFilePostQuery> videoInfoFilePostMapper;

	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private UserDailyLimitComponent userDailyLimitComponent;
	@Resource
	private VideoTransferEventProducer videoTransferEventProducer;
	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<VideoInfoPost> findListByParam(VideoInfoPostQuery param) {
		return this.videoInfoPostMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoInfoPostQuery param) {
		return this.videoInfoPostMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<VideoInfoPost> findListByPage(VideoInfoPostQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoInfoPost> list = this.findListByParam(param);
		PaginationResultVO<VideoInfoPost> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(VideoInfoPost bean) {
		return this.videoInfoPostMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoInfoPost>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoPostMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoInfoPost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoInfoPostMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 VideoId查询
	 */
	@Override
	public VideoInfoPost getVideoInfoPostByVideoId(String videoId) {
		return this.videoInfoPostMapper.selectByVideoId(videoId);
	}

	/**
	 * @description 根据 VideoId更新
	 */
	@Override
	public Integer updateVideoInfoPostByVideoId(VideoInfoPost bean, String videoId) {
		return this.videoInfoPostMapper.updateByVideoId(bean, videoId);
	}

	/**
	 * @description 根据 VideoId删除
	 */
	@Override
	public Integer deleteVideoInfoPostByVideoId(String videoId) {
		return this.videoInfoPostMapper.deleteByVideoId(videoId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePostVideoInfo(VideoInfoPostDTO videoInfoPostDTO) {
		VideoInfoPost videoInfoPost = BeanUtil.toBean(videoInfoPostDTO, VideoInfoPost.class);
		String userId = videoInfoPost.getUserId();
		String videoID = videoInfoPost.getVideoId();
		String uploadFileList = videoInfoPostDTO.getUploadFileList();



		List<VideoInfoFilePost> uploadFilesInfo = JsonUtils.convertJsonArray2List(uploadFileList, VideoInfoFilePost.class);
		boolean isNewPost = StringTools.isEmpty(videoInfoPostDTO.getVideoId());

		if (uploadFilesInfo == null || uploadFilesInfo.isEmpty())
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		Integer limitCount = videoRedisComponent.getSysSetting().getVideoPCount();
		if (uploadFilesInfo.size() > limitCount)
			throw new BusinessException("视频分片数超过" + limitCount + "条");

		if (isNewPost) {
			// 只有新投稿才占用每日发布次数，编辑已有稿件不计入当日额度。
			userDailyLimitComponent.checkDailyLimit(userId, UserDailyLimitTypeEnum.POST_VIDEO);
		}

		//删除列表
		List<VideoInfoFilePost> deleteList = new ArrayList<>();
		//新增列表
		List<VideoInfoFilePost> addList = uploadFilesInfo;
		// 根据前端是否传过来videoId来判断是添加还是修改操作
		if (!StringTools.isEmpty(videoInfoPostDTO.getVideoId()))
		{
			//修改操作
			VideoInfoPost videoInfoPostDb = videoInfoPostMapper.selectByVideoId(videoInfoPostDTO.getVideoId());
			//判断修改的视频是否存在
			if (videoInfoPostDb == null)
				throw new BusinessException("投稿视频不存在");
			// 转码中的稿件文件还没有稳定下来，不能直接编辑；待审核稿件允许继续保存，避免重复提交时报参数错误。
			if (ArrayUtils.contains(new Integer[]{VideoStatusEnum.STATUS_0.getStatus()}, videoInfoPostDb.getStatus()))
				throw new BusinessException("视频正在转码中，暂时不能重新发布");

			VideoInfoFilePostQuery query = new VideoInfoFilePostQuery();
			query.setVideoId(videoID);
			query.setUserId(videoInfoPostDTO.getUserId());

			//在数据库中找到已存到数据库中视频列表
			List<VideoInfoFilePost> filesInfoInPostDb = videoInfoFilePostMapper.selectList(query);

			// 编辑旧分 P 时 fileId 才是最稳定的身份标识；uploadId 只作为历史数据兜底。
			Map<String, VideoInfoFilePost> videoInfoFileByFileId = uploadFilesInfo.stream()
					.filter(item -> !StringTools.isEmpty(item.getFileId()))
					.collect(Collectors.toMap(VideoInfoFilePost::getFileId, Function.identity(), (ans1, ans2) -> ans2));
			Map<String, VideoInfoFilePost> videoInfoFileByUploadId = uploadFilesInfo.stream()
					.filter(item -> !StringTools.isEmpty(item.getUploadId()))
					.collect(Collectors.toMap(VideoInfoFilePost::getUploadId, Function.identity(), (ans1, ans2) -> ans2));

			Boolean isUpdateFileName = false;
			for (VideoInfoFilePost filePost : filesInfoInPostDb)
			{
				VideoInfoFilePost isInDb = videoInfoFileByFileId.get(filePost.getFileId());
				if (isInDb == null) {
					isInDb = videoInfoFileByUploadId.get(filePost.getUploadId());
				}
				//在修改视频文件列表中找出被删除了视频文件
				if (isInDb == null)
				{
					deleteList.add(filePost);
					continue;
				}

				//判断是否有文件名改变
				if (!Objects.equals(filePost.getFileName(), isInDb.getFileName()))
				{
					isUpdateFileName = true;
				}
				isInDb.setUpdateType(filePost.getUpdateType());
				isInDb.setTransferResult(filePost.getTransferResult());
				isInDb.setDuration(filePost.getDuration());
			}
			//判断是否改变了文件信息
			Boolean changeVideoInfoPost = isChangeVideoInfoPost(videoInfoPostDTO);

			//通过新提交的文件中找到没有fileId的新增文件
			addList = uploadFilesInfo.stream().filter(fileInfo -> StringTools.isEmpty(fileInfo.getFileId())).collect(Collectors.toList());

			if (addList != null && addList.isEmpty())
			{
				//改为修改状态
				if (isUpdateFileName || changeVideoInfoPost)
					videoInfoPost.setStatus(VideoStatusEnum.STATUS_2.getStatus());
			}else{
				//新增改为默认状态
				videoInfoPost.setStatus(VideoStatusEnum.STATUS_0.getStatus());
			}
			videoInfoPostMapper.updateByVideoId(videoInfoPost, videoID);

		}else{
			//新增操作
			videoID = StringTools.generateRandomStr(Constants.LENGTH_10);
			//操作videoInfoPost文件
			videoInfoPost.setCreateTime(new Date());
			videoInfoPost.setLastUpdateTime(new Date());
			videoInfoPost.setStatus(VideoStatusEnum.STATUS_0.getStatus());
			videoInfoPost.setVideoId(videoID);
			videoInfoPostMapper.insert(videoInfoPost);
		}

		//操作videoInfoFilePost文件
		Integer index = 1;
		for (VideoInfoFilePost file: uploadFilesInfo)
		{
			file.setFileIndex(index++);
			file.setUserId(userId);
			file.setVideoId(videoID);
			if (file.getFileId() == null)
			{
				file.setFileId(StringTools.generateRandomStr(Constants.LENGTH_20));
				file.setUpdateType(VideoFileUpdateTypeEnum.UPDATE.getStatus());
				file.setTransferResult(VideoFileTransferResultEnum.TRANSFER.getStatus());
			}
		}

		videoInfoFilePostMapper.insertOrUpdateBatch(uploadFilesInfo);
		if (deleteList != null && !deleteList.isEmpty())
		{
			List<String> deleteListIds = deleteList.stream().map(VideoInfoFilePost::getFileId).collect(Collectors.toList());
			videoInfoFilePostMapper.delBatchByIds(deleteListIds, userId);
			List<String> filePathList = deleteList.stream().map(VideoInfoFilePost::getFilePath).collect(Collectors.toList());
			videoRedisComponent.addFileList2DelQueue(videoID, filePathList);
		}

		List<VideoTransferEvent> transferEventList = buildTransferEventList(addList, videoID, userId);

		if (isNewPost) {
			userDailyLimitComponent.recordDailyAction(userId, UserDailyLimitTypeEnum.POST_VIDEO);
		}

		if (!transferEventList.isEmpty()) {
			registerTransferEventAfterCommit(transferEventList);
		}

	}

	@Override
	public Integer updateByCondition(VideoInfoPost updateInfoPost, VideoInfoPostQuery postQuery) {
		return videoInfoPostMapper.updateByCondition(updateInfoPost, postQuery);
	}

	@Override
	public VideoAuditCountVO getVideoCountInfo(String userId) {
		VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
		videoInfoPostQuery.setUserId(userId);
		Integer status = VideoStatusEnum.STATUS_3.getStatus();
		videoInfoPostQuery.setStatus(status);
		Integer auditPassCount = this.findCountByParam(videoInfoPostQuery);

		status = VideoStatusEnum.STATUS_4.getStatus();
		videoInfoPostQuery.setStatus(status);
		Integer auditFailCount = this.findCountByParam(videoInfoPostQuery);

		videoInfoPostQuery.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS_3.getStatus(), VideoStatusEnum.STATUS_4.getStatus()});
		videoInfoPostQuery.setStatus(null);
		Integer inProgress = this.findCountByParam(videoInfoPostQuery);
		VideoAuditCountVO videoAuditCountVO = new VideoAuditCountVO();
		videoAuditCountVO.setAuditFailCount(auditFailCount);
		videoAuditCountVO.setInProgress(inProgress);
		videoAuditCountVO.setAuditPassCount(auditPassCount);
		return videoAuditCountVO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveVideoInteraction(VideoInfoPost videoInfoPost) {
		//更新infoPost, info
		VideoInfoPostQuery videoInfoPostQuery = new VideoInfoPostQuery();
		videoInfoPostQuery.setVideoId(videoInfoPost.getVideoId());
		// provider 已经校验过归属，这里仍带上 userId，避免后续复用该方法时放宽更新范围。
		videoInfoPostQuery.setUserId(videoInfoPost.getUserId());
		videoInfoPostMapper.updateByCondition(videoInfoPost, videoInfoPostQuery);

		VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoInfoPost.getVideoId());
		if (videoInfo == null)
			return;
		videoInfo.setInteraction(videoInfoPost.getInteraction());
		videoInfoQuery.setVideoId(videoInfo.getVideoId());
		videoInfoQuery.setUserId(videoInfo.getUserId());
		videoInfoMapper.updateByCondition(videoInfo, videoInfoQuery);
	}

	private Boolean isChangeVideoInfoPost(VideoInfoPostDTO videoInfoPost)
	{
		VideoInfoPost currentVideoInfo = videoInfoPostMapper.selectByVideoId(videoInfoPost.getVideoId());
		// 简介, 标签, 标题, 封面
		return !currentVideoInfo.getTags().equals(videoInfoPost.getTags())
				|| !(currentVideoInfo.getIntroduction() == null ? "": currentVideoInfo.getIntroduction()).equals(videoInfoPost.getIntroduction() == null ? "" : videoInfoPost.getIntroduction())
				|| !currentVideoInfo.getVideoCover().equals(videoInfoPost.getVideoCover())
				|| !currentVideoInfo.getVideoName().equals(videoInfoPost.getVideoName());
	}

	/**
	 * 为新增分 P 组装转码消息。
	 *
	 * <p>消息体只保留消费必须字段，消费者收到后再按 fileId 查数据库最新状态，
	 * 避免把表结构和运行时字段全部耦合到 MQ 契约里。</p>
	 *
	 * @param addList 新增分 P 列表
	 * @param videoId 视频 id
	 * @param userId 用户 id
	 * @return 转码事件列表
	 */
	private List<VideoTransferEvent> buildTransferEventList(List<VideoInfoFilePost> addList, String videoId, String userId) {
		if (addList == null || addList.isEmpty()) {
			return List.of();
		}
		List<VideoTransferEvent> transferEventList = new ArrayList<>(addList.size());
		for (VideoInfoFilePost addFile : addList) {
			addFile.setVideoId(videoId);
			addFile.setUserId(userId);

			VideoTransferEvent event = new VideoTransferEvent();
			event.setEventId(StringTools.generateRandomStr(Constants.LENGTH_20));
			event.setFileId(addFile.getFileId());
			event.setUploadId(addFile.getUploadId());
			event.setVideoId(videoId);
			event.setUserId(userId);
			transferEventList.add(event);
		}
		return transferEventList;
	}

	/**
	 * 在事务提交后发送转码消息。
	 *
	 * <p>这里必须等数据库事务成功提交后再投递 MQ，否则会出现数据库回滚但消费者已经开始转码的脏任务。</p>
	 *
	 * @param transferEventList 待发送的转码消息
	 */
	private void registerTransferEventAfterCommit(List<VideoTransferEvent> transferEventList) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				for (VideoTransferEvent transferEvent : transferEventList) {
					videoTransferEventProducer.sendTransferEvent(transferEvent);
				}
			}
		});
	}

}
