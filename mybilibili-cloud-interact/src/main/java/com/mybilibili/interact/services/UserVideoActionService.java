package com.mybilibili.interact.services;

import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.base.entity.vo.UserCollectionVO;
import com.mybilibili.interact.entity.po.UserVideoAction;

import java.util.List;


/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论Service
 */
public interface UserVideoActionService {

	/**
	 * @description 根据条件查询
	 */
	List<UserVideoAction> findListByParam(UserActionQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(UserActionQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<UserVideoAction> findListByPage(UserActionQuery param);

	/**
	 * @description 新增
	 */
	Integer add(UserVideoAction bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<UserVideoAction>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserVideoAction> listBean);


	/**
	 * @description 根据 ActionId查询
	 */
	UserVideoAction getUserActionByActionId(Integer actionId);

	/**
	 * @description 根据 ActionId更新
	 */
	Integer updateUserActionByActionId(UserVideoAction bean, Integer actionId);

	/**
	 * @description 根据 ActionId删除
	 */
	Integer deleteUserActionByActionId(Integer actionId);

	/**
	 * @description 根据 VideoIdAndActionTypeAndUserId查询
	 */
	UserVideoAction getUserActionByVideoIdAndActionTypeAndUserId(String videoId, Integer actionType, String userId);

	/**
	 * @description 根据 VideoIdAndActionTypeAndUserId删除
	 */
	Integer deleteUserActionByVideoIdAndActionTypeAndUserId(String videoId, Integer actionType, String userId);

	/**
	 * 得到用户对该视频做的action
	 * @param actionQuery 查询action条件
	 * @return 符合所有action类型
	 */
	List<UserActionVO> getUserActionTypeList(UserActionQuery actionQuery);

    Integer sumCoinCount(String userId);

	PaginationResultVO<UserCollectionVO> loadUserCollection(Integer pageNo, String userId);
}
