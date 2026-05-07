package com.mybilibili.video.consumer;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserActionSyncDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = Constants.CLOUD_INTERACT_NAME)
public interface UserVideoActionClient {
    //TODO 带接通收藏视频

    @RequestMapping(Constants.INNER_API_PREFIX + "/getUserActionList")
    PaginationResultVO<UserActionSyncDTO> getUserCollectionVideoList(@RequestParam("pageNo") Integer pageNo,
                                                                     @RequestParam("userId") String userId);
}
