package com.mybilibili.video.consumer;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.po.CategoryInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(Constants.CLOUD_ADMIN_NAME)
public interface CategoryClient {
    @RequestMapping(Constants.INNER_API_PREFIX + "/loadAllCategory")
    List<CategoryInfo> loadAllCategory();
}
