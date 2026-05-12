package com.mybilibili.video.consumer;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.CategoryInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * admin 分类接口客户端。
 *
 * <p>video 服务只关心分类的读取结果，分类维护仍然放在 admin 服务内。
 * contextId 用接口职责命名，避免同一个服务下多个 FeignClient 共用服务名时，
 * 生成相同的 FeignClientSpecification Bean。</p>
 */
@FeignClient(contextId = "adminCategoryClient", name = Constants.CLOUD_ADMIN_NAME)
public interface CategoryClient {

    /**
     * 查询全部视频分类。
     *
     * @return 分类树或分类列表，具体结构以 admin 服务返回值为准
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/loadAllCategory")
    List<CategoryInfoVO> loadAllCategory();
}
