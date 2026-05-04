package com.mybilibili.video.services.impl;

import com.mybilibili.common.component.UserDailyLimitComponent;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.video.entity.po.VideoDanmu;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.video.entity.query.VideoDanmuQuery;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.*;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.video.entity.enums.SearchOrderTypeEnum;
import com.mybilibili.video.mappers.VideoDanmuMapper;
import com.mybilibili.video.mappers.VideoInfoMapper;
import com.mybilibili.video.services.VideoDanmuService;
import com.mybilibili.video.services.VideoEsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * @author amani
 * @since 2026/03/09
 * 视频弹幕Service
 */

@Service("VideoDanmuService")
public class VideoDanmuServiceImpl implements VideoDanmuService {
	@Resource
	private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;

	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Resource
	private VideoEsService videoEsService;
	@Resource
	private AdminConfig adminConfig;
	@Resource
	private UserDailyLimitComponent userDailyLimitComponent;

	/**
	 * 根据条件查询
	 */
	@Override
	public List<VideoDanmu> findListByParam(VideoDanmuQuery param) {
		return this.videoDanmuMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoDanmuQuery param) {
		return this.videoDanmuMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<VideoDanmu> findListByPage(VideoDanmuQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoDanmu> list = this.findListByParam(param);
		PaginationResultVO<VideoDanmu> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoDanmu bean) {
		return this.videoDanmuMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoDanmu>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoDanmuMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoDanmu> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoDanmuMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 DanmuId查询
	 */
	@Override
	public VideoDanmu getVideoDanmuByDanmuId(Integer danmuId) {
		return this.videoDanmuMapper.selectByDanmuId(danmuId);
	}

	/**
	 * 根据 DanmuId更新
	 */
	@Override
	public Integer updateVideoDanmuByDanmuId(VideoDanmu bean, Integer danmuId) {
		return this.videoDanmuMapper.updateByDanmuId(bean, danmuId);
	}

	/**
	 * 根据 DanmuId删除
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer deleteVideoDanmuByDanmuId(Integer danmuId, Boolean isAdmin, String userId) {
		// 1. 校验弹幕是否存在（保命符，防止后面逻辑崩掉）
		VideoDanmu danmu = Optional.ofNullable(videoDanmuMapper.selectByDanmuId(danmuId))
				.orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

		// 2. 权限判定（利用短路逻辑优化性能）
		// 如果是管理员，或者是弹幕作者，直接通过，不查视频表
		boolean canDirectDelete = Boolean.TRUE.equals(isAdmin) || danmu.getUserId().equals(userId);

		if (!canDirectDelete) {
			// 查视频表看是不是 UP 主
			VideoInfo videoInfo = Optional.ofNullable(videoInfoMapper.selectByVideoId(danmu.getVideoId()))
					.orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

			if (!videoInfo.getUserId().equals(userId)) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}

		// 3. 执行删除并返回结果
		return this.videoDanmuMapper.deleteByDanmuId(danmuId);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void postDanmu(VideoDanmu videoDanmu) {
		// 弹幕发送成功后才算占用额度，因此先校验，最后再记录。
		userDailyLimitComponent.checkDailyLimit(videoDanmu.getUserId(), UserDailyLimitTypeEnum.DANMU);

		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoDanmu.getVideoId());
		if (videoInfo == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())){
			throw new BusinessException("up主已经关闭弹幕");
		}

		//借助原子更新解决弹幕发送高并发问题
		videoInfoMapper.updateCount(videoDanmu.getVideoId(), UserActionTypeEnum.VIDEO_DNAMU.getField(), 1);
		this.add(videoDanmu);
		videoEsService.updateCount(adminConfig.getEsIndexVideoName(), videoInfo.getVideoId(), 1, SearchOrderTypeEnum.VIDEO_DANMU.getField());
		userDailyLimitComponent.recordDailyAction(videoDanmu.getUserId(), UserDailyLimitTypeEnum.DANMU);
	}

	@Override
	public List<VideoDanmu> loadDanmu(String fileId, String videoId) {

		VideoInfo videoInfo = videoInfoMapper.selectByVideoId(videoId);

		// 用户是否开通弹幕
		if (videoInfo == null || (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())))
			return Collections.emptyList();

		VideoDanmuQuery danmuQuery = new VideoDanmuQuery();
		danmuQuery.setFileId(fileId);
		danmuQuery.setVideoId(videoId);
		return this.findListByParam(danmuQuery);

	}

}
