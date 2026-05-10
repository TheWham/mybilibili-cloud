package com.mybilibili.video.consumer;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserActionSyncDTO;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserActionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "InteractClientInVideo", name = Constants.CLOUD_INTERACT_NAME)
public interface UserVideoActionClient {

    @GetMapping(Constants.INNER_API_PREFIX + "/getUserActionList")
    PaginationResultVO<UserActionSyncDTO> getUserCollectionVideoList(@RequestParam("pageNo") Integer pageNo,
                                                                     @RequestParam("userId") String userId);


    @GetMapping(Constants.INNER_API_PREFIX + "getUserActionTypeList")
    List<UserActionVO> getUserActionTypeList(@RequestBody UserActionQuery userActionQuery);
}
