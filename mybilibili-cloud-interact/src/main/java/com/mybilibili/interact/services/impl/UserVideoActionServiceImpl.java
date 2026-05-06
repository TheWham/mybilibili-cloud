package com.mybilibili.interact.services.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.base.entity.vo.UserCollectionVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.interact.component.InteractRedisComponent;
import com.mybilibili.interact.consumer.VideoInfoClient;
import com.mybilibili.interact.entity.po.UserVideoAction;
import com.mybilibili.interact.mappers.UserVideoActionMapper;
import com.mybilibili.interact.services.UserVideoActionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论Service
 */

@Service("UserVideoActionService")
public class UserVideoActionServiceImpl implements UserVideoActionService {

	@Resource
	private UserVideoActionMapper<UserVideoAction, UserActionQuery> userVideoActionMapper;
	@Resource
	private VideoInfoClient videoInfoClient;
	@Resource
	private InteractRedisComponent redisComponent;



	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<UserVideoAction> findListByParam(UserActionQuery param) {
		return this.userVideoActionMapper.selectList(param);
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(UserActionQuery param) {
		return this.userVideoActionMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<UserVideoAction> findListByPage(UserActionQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserVideoAction> list = this.findListByParam(param);
		PaginationResultVO<UserVideoAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(UserVideoAction bean) {
		return this.userVideoActionMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<UserVideoAction>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoActionMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserVideoAction> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userVideoActionMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 ActionId查询
	 */
	@Override
	public UserVideoAction getUserActionByActionId(Integer actionId) {
		return this.userVideoActionMapper.selectByActionId(actionId);
	}

	/**
	 * @description 根据 ActionId更新
	 */
	@Override
	public Integer updateUserActionByActionId(UserVideoAction bean, Integer actionId) {
		return this.userVideoActionMapper.updateByActionId(bean, actionId);
	}

	/**
	 * @description 根据 ActionId删除
	 */
	@Override
	public Integer deleteUserActionByActionId(Integer actionId) {
		return this.userVideoActionMapper.deleteByActionId(actionId);
	}


	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId查询
	 */
	@Override
	public UserVideoAction getUserActionByVideoIdAndActionTypeAndUserId(String videoId, Integer actionType, String userId) {
		return this.userVideoActionMapper.selectByVideoIdAndActionTypeAndUserId(videoId, actionType, userId);
	}

	/**
	 * @description 根据 VideoIdAndActionTypeAndUserId删除
	 */
	@Override
	public Integer deleteUserActionByVideoIdAndActionTypeAndUserId(String videoId,Integer actionType, String userId) {
		return this.userVideoActionMapper.deleteByVideoIdAndActionTypeAndUserId(videoId, actionType, userId);
	}


	@Override
	public List<UserActionVO> getUserActionTypeList(UserActionQuery actionQuery) {
		if (actionQuery == null
				|| actionQuery.getVideoId() == null
				|| actionQuery.getUserId() == null
				|| actionQuery.getUserActionTypeList() == null
				|| actionQuery.getUserActionTypeList().length == 0) {
			return userVideoActionMapper.selectActionTypeList(actionQuery);
		}

		List<UserActionVO> result = new ArrayList<>();
		List<Integer> dbMissTypeList = new ArrayList<>();
		for (Integer actionType : actionQuery.getUserActionTypeList()) {
			if (actionType == null) {
				continue;
			}
			// 视频详情页刷新后要马上看到点赞/收藏/投币高亮，
			// 这里优先读 Redis 里的动作状态，避免等异步同步 MySQL 后才有结果。
			// Redis 状态值 > 0 代表当前高亮，= 0 代表最近一次操作是取消，此时不能再回源 DB 把旧高亮补回来。
			Integer actionStatus = redisComponent.getVideoActionStatus(actionQuery.getUserId(), actionQuery.getVideoId(), actionType);
			if (actionStatus != null && actionStatus > 0) {
				UserActionVO userActionVO = new UserActionVO();
				userActionVO.setActionType(actionType);
				result.add(userActionVO);
				continue;
			}
			if (actionStatus != null && actionStatus == 0) {
				continue;
			}
			dbMissTypeList.add(actionType);
		}

		if (dbMissTypeList.isEmpty()) {
			return result;
		}

		UserActionQuery dbQuery = new UserActionQuery();
		dbQuery.setUserId(actionQuery.getUserId());
		dbQuery.setVideoId(actionQuery.getVideoId());
		dbQuery.setUserActionTypeList(dbMissTypeList.toArray(new Integer[0]));
		List<UserActionVO> dbActionList = userVideoActionMapper.selectActionTypeList(dbQuery);
		if (dbActionList == null || dbActionList.isEmpty()) {
			return result;
		}

		// Redis 没命中的历史数据，回源一次数据库后顺手把状态补回缓存。
		for (UserActionVO userActionVO : dbActionList) {
			redisComponent.saveVideoActionStatus(actionQuery.getUserId(), actionQuery.getVideoId(), userActionVO.getActionType(), 1);
		}
		result.addAll(dbActionList);
		return result;
	}

	@Override
	public Integer sumCoinCount(String userId) {
		return userVideoActionMapper.sumCoinCount(userId);
	}

	@Override
	public PaginationResultVO<UserCollectionVO> loadUserCollection(Integer pageNo, String userId) {
		UserActionQuery actionQuery = new UserActionQuery();
		actionQuery.setUserId(userId);
		actionQuery.setPageNo(pageNo);
		actionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
		actionQuery.setOrderBy("v.action_time desc");
		PaginationResultVO<UserVideoAction> userCollectionVideoPage = this.findListByPage(actionQuery);

		if (userCollectionVideoPage == null || userCollectionVideoPage.getList() == null || userCollectionVideoPage.getList().isEmpty()) {
			return new PaginationResultVO<>(0, PageSize.SIZE15.getSize(), pageNo == null ? 1 : pageNo, 0, Collections.emptyList());
		}

		List<UserVideoAction> userCollectionVideoList = userCollectionVideoPage.getList();
		Map<String, Date> videoIdTimeMap = userCollectionVideoList.stream()
				.collect(Collectors.toMap(UserVideoAction::getVideoId, UserVideoAction::getActionTime, (left, right) -> left));
		List<String> userCollectionIds = userCollectionVideoList.stream().map(UserVideoAction::getVideoId).collect(Collectors.toList());

		// 收藏页要保持“收藏时间倒序”的展示顺序，不能直接按 video 服务 in 查询结果返回。
		List<UserCollectionVO> unOrderVideoInfos = videoInfoClient.loadCollectionVideoInfo(userCollectionIds);
		if (unOrderVideoInfos == null || unOrderVideoInfos.isEmpty()) {
			return new PaginationResultVO<>(userCollectionVideoPage.getTotalCount(), userCollectionVideoPage.getPageSize(),
					userCollectionVideoPage.getPageNo(), userCollectionVideoPage.getPageTotal(), Collections.emptyList());
		}
		Map<String, UserCollectionVO> videoCollectMap = unOrderVideoInfos.stream()
				.collect(Collectors.toMap(UserCollectionVO::getVideoId, videoInfo -> videoInfo, (left, right) -> left));
		List<UserCollectionVO> finalOrderVideoList = userCollectionIds.stream()
				.map(videoCollectMap::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		List<UserCollectionVO> userCollectionVOList = BeanUtil.copyToList(finalOrderVideoList, UserCollectionVO.class);
		userCollectionVOList.forEach(userCollectionVO -> userCollectionVO.setActionTime(videoIdTimeMap.get(userCollectionVO.getVideoId())));

		PaginationResultVO<UserCollectionVO> userCollectionPage = new PaginationResultVO<>();
		userCollectionPage.setTotalCount(userCollectionVideoPage.getTotalCount());
		userCollectionPage.setPageTotal(userCollectionVideoPage.getPageTotal());
		userCollectionPage.setPageSize(userCollectionVideoPage.getPageSize());
		userCollectionPage.setPageNo(userCollectionVideoPage.getPageNo());
		userCollectionPage.setList(userCollectionVOList);
		return userCollectionPage;
	}

}
