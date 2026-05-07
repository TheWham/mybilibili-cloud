package com.mybilibili.interact.mappers;

import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户评论行为 Mapper。
 */
public interface UserCommentActionMapper<T, R> extends BaseMapper {

    T selectByActionId(@Param("actionId") Integer actionId);

    Integer updateByActionId(@Param("bean") T t, @Param("actionId") Integer actionId);

    Integer deleteByActionId(@Param("actionId") Integer actionId);

    T selectByCommentIdAndUserId(@Param("commentId") Integer commentId, @Param("userId") String userId);

    Integer updateByCommentIdAndUserId(@Param("bean") T t, @Param("commentId") Integer commentId, @Param("userId") String userId);

    Integer deleteByCommentIdAndUserId(@Param("commentId") Integer commentId, @Param("userId") String userId);

    Integer insertIgnore(@Param("bean") T userCommentAction);

    Integer selectActionTypeForUpdate(@Param("commentId") Integer commentId, @Param("userId") String userId);

    List<UserActionVO> selectActionTypeList(@Param("query") R actionQuery);
}
