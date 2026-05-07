package com.mybilibili.interact.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserActionSyncDTO;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.interact.entity.po.UserVideoAction;
import com.mybilibili.interact.services.UserVideoActionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class UserVideoActionApi extends ABaseController {

    @Resource
    private UserVideoActionService userVideoActionService;

    @RequestMapping("/getUserActionList")
    PaginationResultVO<UserActionSyncDTO> getUserCollectionVideoList(@RequestParam(value = "pageNo", required = false) Integer pageNo, @RequestParam("userId") String userId) {
        UserActionQuery actionQuery = new UserActionQuery();
        actionQuery.setUserId(userId);
        actionQuery.setPageNo(pageNo);
        actionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        actionQuery.setOrderBy("v.action_time desc");
        PaginationResultVO<UserVideoAction> userCollectionVideoPage = userVideoActionService.findListByPage(actionQuery);

        if (userCollectionVideoPage == null || userCollectionVideoPage.getList() == null || userCollectionVideoPage.getList().isEmpty()) {
            return new PaginationResultVO<>(0, PageSize.SIZE15.getSize(), pageNo == null ? 1 : pageNo, 0, Collections.emptyList());
        }
        PaginationResultVO<UserActionSyncDTO> userCollectionVideoSyncPage = po2Dto(userCollectionVideoPage);
        return userCollectionVideoSyncPage;
    }

    private PaginationResultVO<UserActionSyncDTO> po2Dto(PaginationResultVO<UserVideoAction> po)
    {
        PaginationResultVO<UserActionSyncDTO> userCollectionVideoSyncPage = new PaginationResultVO<>();
        userCollectionVideoSyncPage.setPageSize(po.getPageSize());
        userCollectionVideoSyncPage.setPageTotal(po.getPageTotal());
        userCollectionVideoSyncPage.setTotalCount(po.getTotalCount());
        userCollectionVideoSyncPage.setPageNo(po.getPageNo());

        List<UserVideoAction> userCollectionVideoList = po.getList();
        List<UserActionSyncDTO> userActionSyncDTOS = BeanUtil.copyToList(userCollectionVideoList, UserActionSyncDTO.class);
        userCollectionVideoSyncPage.setList(userActionSyncDTOS);
        return userCollectionVideoSyncPage;
    }

}
