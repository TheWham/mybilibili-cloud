package com.mybilibili.admin.provider;

import com.mybilibili.admin.services.CategoryInfoService;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.admin.entity.po.CategoryInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author amani
 * @since 2026.4.27
 */

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class CategoryApi {

    @Resource
    private CategoryInfoService categoryInfoService;

    @RequestMapping("/loadAllCategory")
    public List<CategoryInfo> loadAllCategory()
    {
         return categoryInfoService.loadAllCategory();
    }
}
