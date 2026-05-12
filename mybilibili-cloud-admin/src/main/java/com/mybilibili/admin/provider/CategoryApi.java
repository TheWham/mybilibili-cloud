package com.mybilibili.admin.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.admin.entity.po.CategoryInfo;
import com.mybilibili.admin.services.CategoryInfoService;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.CategoryInfoVO;
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
    public List<CategoryInfoVO> loadAllCategory()
    {
        List<CategoryInfo> categoryInfos = categoryInfoService.loadAllCategory();
        List<CategoryInfoVO> categoryDTOS = BeanUtil.copyToList(categoryInfos, CategoryInfoVO.class);
        return categoryDTOS;
    }
}
