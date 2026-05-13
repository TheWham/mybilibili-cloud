package com.mybilibili.interact.provider;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.base.entity.vo.UserCollectionVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.interact.consumer.VideoInfoClient;
import com.mybilibili.interact.entity.po.UserVideoAction;
import com.mybilibili.interact.services.UserVideoActionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class UserVideoActionInInteractApi {

    @Resource
    private UserVideoActionService userVideoActionService;

    @Resource
    private VideoInfoClient videoInfoClient;


    @PostMapping("/getUserActionTypeList")
    public List<UserActionVO> getUserActionTypeList(@RequestBody UserActionQuery actionQuery) {
        return userVideoActionService.getUserActionTypeList(actionQuery);
    }


    @GetMapping("/loadUserCollection")
    PaginationResultVO<UserCollectionVO> getUserCollectionVideoList(Integer pageNo,
                                                                    @NotEmpty String userId)
    {
        UserActionQuery actionQuery = new UserActionQuery();
        actionQuery.setUserId(userId);
        actionQuery.setPageNo(pageNo);
        actionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        actionQuery.setOrderBy("v.action_time desc");
        PaginationResultVO<UserVideoAction> userCollectionVideoPage = this.userVideoActionService.findListByPage(actionQuery);

        if (userCollectionVideoPage == null || userCollectionVideoPage.getList() == null || userCollectionVideoPage.getList().isEmpty()) {
            return new PaginationResultVO<>(0, PageSize.SIZE15.getSize(), pageNo == null ? 1 : pageNo, 0, Collections.emptyList());
        }

        List<UserVideoAction> userCollectionVideoList = userCollectionVideoPage.getList();
        Map<String, Date> videoIdTimeMap = userCollectionVideoList.stream()
                .collect(Collectors.toMap(UserVideoAction::getVideoId, UserVideoAction::getActionTime, (left, right) -> left));
        List<String> userCollectionIds = userCollectionVideoList.stream().map(UserVideoAction::getVideoId).collect(Collectors.toList());

        // 收藏页要保持“收藏时间倒序”的展示顺序，不能直接按数据库 in 查询结果返回。
        List<UserCollectionVO> userCollectionVOList = videoInfoClient.loadCollectionVideoInfo(userCollectionIds);
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
