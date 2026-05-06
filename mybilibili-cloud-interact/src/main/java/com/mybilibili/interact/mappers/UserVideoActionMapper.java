package com.mybilibili.interact.mappers;


import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论
 */
public interface UserVideoActionMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 ActionId查询
	 */
	T selectByActionId(@Param("actionId") Integer actionId);

	/**
	 * @description 根据 ActionId更新
	 */
	Integer updateByActionId(@Param("bean") T t, @Param("actionId") Integer actionId);

	/**
	 * @description 根据 ActionId删除
	 */
	Integer deleteByActionId(@Param("actionId") Integer actionId);


	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId查询
	 */
	T selectByVideoIdAndActionTypeAndUserId(@Param("videoId") String videoId, @Param("actionType") Integer actionType, @Param("userId") String userId);

	/**
	 * @description 根据 VideoIdAndCommentIdAndActionTypeAndUserId删除
	 */
	Integer deleteByVideoIdAndActionTypeAndUserId(@Param("videoId") String videoId, @Param("actionType") Integer actionType, @Param("userId") String userId);

    Integer selectSingleAction(@Param("query") R actionQuery);

	Integer insertIgnore(@Param("bean") T t);

	List<UserActionVO> selectActionTypeList(@Param("query") R actionQuery);

    Integer sumCoinCount(@Param("videoUserId") String videoUserId);
}
