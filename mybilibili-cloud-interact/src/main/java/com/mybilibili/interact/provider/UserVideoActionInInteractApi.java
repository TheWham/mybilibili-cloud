package com.mybilibili.interact.provider;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.interact.services.UserVideoActionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class UserVideoActionInInteractApi {

    @Resource
    private UserVideoActionService userVideoActionService;

    @PostMapping("/getUserActionTypeList")
    public List<UserActionVO> getUserActionTypeList(@RequestBody UserActionQuery actionQuery) {
        return userVideoActionService.getUserActionTypeList(actionQuery);
    }
}
