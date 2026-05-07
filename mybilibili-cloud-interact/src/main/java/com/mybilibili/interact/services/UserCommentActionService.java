package com.mybilibili.interact.services;

import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.interact.entity.po.UserCommentAction;

import java.util.List;

/**
 * 用户评论行为 Service。
 */
public interface UserCommentActionService {

    List<UserCommentAction> findListByParam(UserActionQuery param);

    Integer findCountByParam(UserActionQuery param);

    PaginationResultVO<UserCommentAction> findListByPage(UserActionQuery param);

    Integer add(UserCommentAction bean);

    Integer addBatch(List<UserCommentAction> listBean);

    Integer addOrUpdateBatch(List<UserCommentAction> listBean);

    UserCommentAction getUserCommentActionByActionId(Integer actionId);

    Integer updateUserCommentActionByActionId(UserCommentAction bean, Integer actionId);

    Integer deleteUserCommentActionByActionId(Integer actionId);

    UserCommentAction getUserCommentActionByCommentIdAndUserId(Integer commentId, String userId);

    Integer updateUserCommentActionByCommentIdAndUserId(UserCommentAction bean, Integer commentId, String userId);

    Integer deleteUserCommentActionByCommentIdAndUserId(Integer commentId, String userId);
}
