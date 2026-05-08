package com.mybilibili.user.services;


import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.user.entity.po.UserFocus;
import com.mybilibili.user.entity.query.UserFocusQuery;

import java.util.List;

public interface UserFocusService {

    List<UserFocus> findListByParam(UserFocusQuery param);
    Integer findCountByParam(UserFocusQuery param);
    PaginationResultVO<UserFocus> findListByPage(UserFocusQuery param);
    Integer add(UserFocus bean);
    Integer addBatch(List<UserFocus> listBean);
    Integer addOrUpdateBatch(List<UserFocus> listBean);
    UserFocus getUserFocusByUserIdAndUserFocusId(String userId, String userFocusId);
    Integer updateUserFocusByUserIdAndUserFocusId(UserFocus bean, String userId, String userFocusId);
    Integer deleteUserFocusByUserIdAndUserFocusId(String userId, String userFocusId);
    void focus(String focusUserId, String userId);
    void cancelFocus(String focusUserId, String userId);
    Integer selectHaveFocus(String userId, String focusUserId);
}
