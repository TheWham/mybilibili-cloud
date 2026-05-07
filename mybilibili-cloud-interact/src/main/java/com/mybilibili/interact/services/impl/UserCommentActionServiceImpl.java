package com.mybilibili.interact.services.impl;

import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.interact.entity.po.UserCommentAction;
import com.mybilibili.interact.mappers.UserCommentActionMapper;
import com.mybilibili.interact.services.UserCommentActionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户评论行为 Service。
 */
@Service("UserCommentActionService")
public class UserCommentActionServiceImpl implements UserCommentActionService {

    @Resource
    private UserCommentActionMapper<UserCommentAction, UserActionQuery> userCommentActionMapper;

    @Override
    public List<UserCommentAction> findListByParam(UserActionQuery param) {
        return userCommentActionMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(UserActionQuery param) {
        return userCommentActionMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<UserCommentAction> findListByPage(UserActionQuery param) {
        Integer count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserCommentAction> list = findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(UserCommentAction bean) {
        return userCommentActionMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<UserCommentAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userCommentActionMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<UserCommentAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return userCommentActionMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public UserCommentAction getUserCommentActionByActionId(Integer actionId) {
        return userCommentActionMapper.selectByActionId(actionId);
    }

    @Override
    public Integer updateUserCommentActionByActionId(UserCommentAction bean, Integer actionId) {
        return userCommentActionMapper.updateByActionId(bean, actionId);
    }

    @Override
    public Integer deleteUserCommentActionByActionId(Integer actionId) {
        return userCommentActionMapper.deleteByActionId(actionId);
    }

    @Override
    public UserCommentAction getUserCommentActionByCommentIdAndUserId(Integer commentId, String userId) {
        return userCommentActionMapper.selectByCommentIdAndUserId(commentId, userId);
    }

    @Override
    public Integer updateUserCommentActionByCommentIdAndUserId(UserCommentAction bean, Integer commentId, String userId) {
        return userCommentActionMapper.updateByCommentIdAndUserId(bean, commentId, userId);
    }

    @Override
    public Integer deleteUserCommentActionByCommentIdAndUserId(Integer commentId, String userId) {
        return userCommentActionMapper.deleteByCommentIdAndUserId(commentId, userId);
    }
}
