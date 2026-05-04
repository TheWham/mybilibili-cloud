package com.mybilibili.video.services.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.po.VideoPlayHistory;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.entity.query.VideoPlayHistoryQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.video.entity.vo.VideoHistoryVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.video.mappers.VideoInfoMapper;
import com.mybilibili.video.mappers.VideoPlayHistoryMapper;
import com.mybilibili.video.services.VideoPlayHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史Service
 */

@Slf4j
@Service("VideoPlayHistoryService")
public class VideoPlayHistoryServiceImpl implements VideoPlayHistoryService {
	@Resource
	private VideoPlayHistoryMapper<VideoPlayHistory, VideoPlayHistoryQuery> videoPlayHistoryMapper;
	@Resource
	private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
	@Resource
	private VideoRedisComponent videoRedisComponent;


	/**
	 * 根据条件查询
	 */
	@Override
	public List<VideoPlayHistory> findListByParam(VideoPlayHistoryQuery param) {
		return this.videoPlayHistoryMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(VideoPlayHistoryQuery param) {
		return this.videoPlayHistoryMapper.selectCount(param);
	}

	/**
	 * 分页查询
	 */
	@Override
	public PaginationResultVO<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null?PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoPlayHistory> list = this.findListByParam(param);
		PaginationResultVO<VideoPlayHistory> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}


	@Override
	public PaginationResultVO<VideoHistoryVO> loadHistoryByPage(String userId, Integer pageNo) {
		int currentPageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
		int pageSize = PageSize.SIZE30.getSize();

		// 历史页优先查 Redis。
		// Redis 里保存的是用户当前最新历史，能保证用户刚退出播放器时页面就能看到最新记录。
		Long totalCount = videoRedisComponent.getVideoHistoryCount(userId);
		if (totalCount != null && totalCount > 0) {
			int pageTotal = (int) Math.ceil((double) totalCount / pageSize);
			long start = (long) (currentPageNo - 1) * pageSize;

			if (start >= totalCount) {
				return new PaginationResultVO<>(totalCount.intValue(), pageSize, currentPageNo, pageTotal, new ArrayList<>());
			}
			long end = Math.min(start + pageSize - 1, totalCount - 1);
			//每次获取30条历史记录
			Set<ZSetOperations.TypedTuple<String>> historyTupleSet = videoRedisComponent.getVideoHistoryWithScoresByPage(userId, start, end);

			if (historyTupleSet == null || historyTupleSet.isEmpty()) {
				return new PaginationResultVO<>(totalCount.intValue(), pageSize, currentPageNo, pageTotal, new ArrayList<>());
			}

			Map<String, Date> lastUpdateTimeMap = new LinkedHashMap<>(historyTupleSet.size());
			List<String> videoHistoryIds = new ArrayList<>(historyTupleSet.size());

			for (ZSetOperations.TypedTuple<String> historyTuple : historyTupleSet) {
				if (historyTuple == null || historyTuple.getValue() == null) {
					continue;
				}
				String videoId = historyTuple.getValue();
				videoHistoryIds.add(videoId);
				if (historyTuple.getScore() != null) {
					lastUpdateTimeMap.put(videoId, new Date(historyTuple.getScore().longValue()));
				}
			}
			List<VideoHistoryVO> list = this.loadHistory(videoHistoryIds, lastUpdateTimeMap);
			return new PaginationResultVO<>(totalCount.intValue(), pageSize, currentPageNo, pageTotal, list);
		}

		// Redis 没命中时再走数据库兜底，避免历史页因为缓存失效直接空掉。
		return getHistoryListInDB(userId, currentPageNo, pageSize);
	}

	private List<VideoHistoryVO> loadHistory(List<String> videoHistoryIds, Map<String, Date> lastUpdateTimeMap) {
		if (videoHistoryIds == null || videoHistoryIds.isEmpty())
			return Collections.emptyList();

		List<VideoInfo> videoInfos = videoInfoMapper.selectByIds(videoHistoryIds);
		if (videoInfos == null || videoInfos.isEmpty())
			return Collections.emptyList();

		Map<String, VideoInfo> map = videoInfos.stream().collect(Collectors.toMap(VideoInfo::getVideoId, Function.identity(), (data1, data2)->data2));
		List<VideoHistoryVO> ans = new ArrayList<>(videoHistoryIds.size());
		for (String id : videoHistoryIds)
		{
			VideoInfo videoInfo = map.get(id);
			if (videoInfo != null)
			{
				VideoHistoryVO vo = BeanUtil.toBean(videoInfo, VideoHistoryVO.class);
				// 历史页展示的观看时间要跟 Redis/数据库里的最后观看时间保持一致，
				// 不能直接复用视频表自己的更新时间。
				if (lastUpdateTimeMap != null) {
					vo.setLastUpdateTime(lastUpdateTimeMap.get(id));
				}
				ans.add(vo);
			}
		}
		return ans;
	}

	private PaginationResultVO<VideoHistoryVO> getHistoryListInDB(String userId, Integer currentPageNo, Integer pageSize){
		VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
		videoPlayHistoryQuery.setUserId(userId);
		videoPlayHistoryQuery.setPageNo(currentPageNo);
		videoPlayHistoryQuery.setPageSize(pageSize);
		videoPlayHistoryQuery.setOrderBy("v.last_update_time desc");
		PaginationResultVO<VideoPlayHistory> pageResult = this.findListByPage(videoPlayHistoryQuery);
		List<VideoPlayHistory> historyList = pageResult.getList();
		if (historyList == null || historyList.isEmpty()) {
			return new PaginationResultVO<>(pageResult.getTotalCount(), pageResult.getPageSize(), pageResult.getPageNo(), pageResult.getPageTotal(), new ArrayList<>());
		}

		List<String> videoHistoryIds = new ArrayList<>(historyList.size());
		Map<String, Date> lastUpdateTimeMap = new LinkedHashMap<>(historyList.size());
		for (VideoPlayHistory history : historyList) {
			if (history == null || history.getVideoId() == null) {
				continue;
			}
			videoHistoryIds.add(history.getVideoId());
			lastUpdateTimeMap.put(history.getVideoId(), history.getLastUpdateTime());
		}
		List<VideoHistoryVO> list = this.loadHistory(videoHistoryIds, lastUpdateTimeMap);
		return new PaginationResultVO<>(pageResult.getTotalCount(), pageResult.getPageSize(), pageResult.getPageNo(), pageResult.getPageTotal(), list);
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoPlayHistory bean) {
		return this.videoPlayHistoryMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoPlayHistory>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoPlayHistoryMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoPlayHistory> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoPlayHistoryMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据 UserIdAndVideoId查询
	 */
	@Override
	public VideoPlayHistory getVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId) {
		return this.videoPlayHistoryMapper.selectByUserIdAndVideoId(userId, videoId);
	}

	/**
	 * 根据 UserIdAndVideoId更新
	 */
	@Override
	public Integer updateVideoPlayHistoryByUserIdAndVideoId(VideoPlayHistory bean, String userId, String videoId) {
		return this.videoPlayHistoryMapper.updateByUserIdAndVideoId(bean, userId, videoId);
	}

	/**
	 * 根据 UserIdAndVideoId删除
	 */
	@Override
	public Integer deleteVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId) {
		return this.videoPlayHistoryMapper.deleteByUserIdAndVideoId(userId, videoId);
	}

	/**
	 * 根据 userId 删除当前用户全部播放历史
	 */
	@Override
	public Integer deleteVideoPlayHistoryByUserId(String userId) {
		return this.videoPlayHistoryMapper.deleteByUserId(userId);
	}

	/**
	 * 删除早于指定时间的历史记录
	 */
	@Override
	public Integer deleteVideoPlayHistoryBefore(Date lastUpdateTime) {
		return this.videoPlayHistoryMapper.deleteByLastUpdateTimeBefore(lastUpdateTime);
	}

	@Override
	public Integer delHistory(String videoId, String userId) {
		VideoPlayHistoryQuery videoPlayHistoryQuery = new VideoPlayHistoryQuery();
		videoPlayHistoryQuery.setUserId(userId);
		videoPlayHistoryQuery.setVideoId(videoId);
		Integer count = this.videoPlayHistoryMapper.selectCount(videoPlayHistoryQuery);
		if (count == 0)
			throw new BusinessException("历史记录不存在");

		Integer deleteAns = this.videoPlayHistoryMapper.deleteByUserIdAndVideoId(userId, videoId);
		Long ans = videoRedisComponent.delHistory(userId, videoId);
		if (ans == 0)
			log.error("缓存删除失败");
		return deleteAns;
	}

	@Override
	public Integer cleanHistory(String userId) {
		Integer count = this.videoPlayHistoryMapper.deleteByUserId(userId);
		this.videoRedisComponent.cleanHistory(userId);
		return count;
	}


}
