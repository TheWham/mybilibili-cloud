package com.mybilibili.search.controller;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.entity.vo.VideoSearchResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.search.component.SearchRedisComponent;
import com.mybilibili.search.service.VideoEsService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 搜索服务对外入口。
 */
@RestController
@RequestMapping("/search")
public class SearchController extends ABaseController {

    @Resource
    private SearchRedisComponent redisComponent;

    @Resource
    private VideoEsService videoEsService;

    @RequestMapping("/search")
    public ResponseVO search(Integer pageNo, @NotEmpty String keyword, Integer orderType)
    {
        redisComponent.saveKeyword(keyword);
        PaginationResultVO<VideoSearchResultVO> search = videoEsService.search(true, keyword, orderType, pageNo, PageSize.SIZE30.getSize());
        return getSuccessResponseVO(search);
    }

    @RequestMapping("/getSearchKeywordTop")
    public ResponseVO getSearchKeywordTop()
    {
        List<String> keywordTopList = redisComponent.getSearchKeywordTop(Constants.LENGTH_10);
        return getSuccessResponseVO(keywordTopList);
    }

}
