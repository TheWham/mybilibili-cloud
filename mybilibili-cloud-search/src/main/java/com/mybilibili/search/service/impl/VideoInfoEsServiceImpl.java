package com.mybilibili.search.service.impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoSearchCountUpdateDTO;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoSearchResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.SearchOrderTypeEnum;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.search.component.SearchRedisComponent;
import com.mybilibili.search.consumer.UserInfoClient;
import com.mybilibili.search.consumer.VideoInfoClient;
import com.mybilibili.search.entity.dto.VideoInfoEsDTO;
import com.mybilibili.search.service.VideoEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 视频 ES 索引服务。
 *
 * <p>search 模块只认 base 中的 DTO/VO，不直接依赖 video、user 的 PO 和 Mapper。
 * 微服务拆分后，这样可以避免表结构变化穿透到搜索服务。</p>
 *
 * @author amani
 */
@Slf4j
@Service("videoEsService")
public class VideoInfoEsServiceImpl implements VideoEsService {

    private static final Integer REBUILD_PAGE_SIZE = Constants.LENGTH_1000;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private AdminConfig adminConfig;

    @Resource
    private SearchRedisComponent redisComponent;

    @Resource
    private UserInfoClient userInfoClient;

    @Resource
    private VideoInfoClient videoInfoClient;

    @Override
    public void saveDoc(VideoInfoDTO videoInfo) {
        checkVideoId(videoInfo);
        try {
            if (isExistKey(videoInfo.getVideoId())) {
                updateDoc(videoInfo);
                return;
            }
            VideoInfoEsDTO videoInfoEsDTO = toEsDTO(videoInfo);
            elasticsearchClient.index(request -> request
                    .index(adminConfig.getEsIndexVideoName())
                    .id(videoInfoEsDTO.getVideoId())
                    .document(videoInfoEsDTO));
        } catch (Exception e) {
            log.error("保存视频搜索索引失败, videoId={}", videoInfo.getVideoId(), e);
            throw new BusinessException("保存视频搜索索引失败", e);
        }
    }

    @Override
    public void updateDoc(VideoInfoDTO videoInfo) {
        checkVideoId(videoInfo);
        Map<String, Object> updateMap = buildUpdateMap(toEsDTO(videoInfo));
        if (updateMap.isEmpty()) {
            return;
        }
        try {
            elasticsearchClient.update(update -> update
                            .index(adminConfig.getEsIndexVideoName())
                            .id(videoInfo.getVideoId())
                            .doc(updateMap)
                            .upsert(toEsDTO(videoInfo)),
                    VideoInfoEsDTO.class);
        } catch (Exception e) {
            log.error("更新视频搜索索引失败, videoId={}", videoInfo.getVideoId(), e);
            throw new BusinessException("更新视频搜索索引失败", e);
        }
    }

    @Override
    public void deleteDoc(String videoId) {
        if (StringTools.isEmpty(videoId)) {
            throw new BusinessException("视频id不能为空");
        }
        try {
            elasticsearchClient.delete(delete -> delete
                    .index(adminConfig.getEsIndexVideoName())
                    .id(videoId));
        } catch (Exception e) {
            log.error("删除视频搜索索引失败, videoId={}", videoId, e);
            throw new BusinessException("删除视频搜索索引失败", e);
        }
    }

    @Override
    public void updateCount(VideoSearchCountUpdateDTO countUpdateDTO) {
        if (countUpdateDTO == null
                || StringTools.isEmpty(countUpdateDTO.getVideoId())
                || countUpdateDTO.getChangeCount() == null) {
            throw new BusinessException("视频计数更新参数不完整");
        }
        SearchOrderTypeEnum orderTypeEnum = SearchOrderTypeEnum.getEnum(countUpdateDTO.getOrderType());
        if (orderTypeEnum == null) {
            throw new BusinessException("不支持的搜索计数字段");
        }

        String field = orderTypeEnum.getField();
        try {
            elasticsearchClient.update(update -> update
                            .index(adminConfig.getEsIndexVideoName())
                            .id(countUpdateDTO.getVideoId())
                            .script(script -> script
                                    .inline(inline -> inline
                                            .source("ctx._source." + field + " = Math.max((ctx._source." + field + " == null ? 0 : ctx._source." + field + ") + params.count, 0)")
                                            .params("count", JsonData.of(countUpdateDTO.getChangeCount())))),
                    Void.class);
        } catch (Exception e) {
            log.error("更新视频搜索计数失败, videoId={}, field={}, changeCount={}",
                    countUpdateDTO.getVideoId(), field, countUpdateDTO.getChangeCount(), e);
            throw new BusinessException("更新视频搜索计数失败", e);
        }
    }

    @Override
    public PaginationResultVO<VideoSearchResultVO> search(Boolean highlight,
                                                          String keyword,
                                                          Integer orderType,
                                                          Integer pageNo,
                                                          Integer pageSize) {
        if (StringTools.isEmpty(keyword)) {
            return new PaginationResultVO<>(0, defaultPageSize(pageSize), defaultPageNo(pageNo), 0, Collections.emptyList());
        }

        int realPageNo = defaultPageNo(pageNo);
        int realPageSize = defaultPageSize(pageSize);
        try {
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(adminConfig.getEsIndexVideoName())
                    .from((realPageNo - 1) * realPageSize)
                    .size(realPageSize);

            searchBuilder.query(query -> query
                    .multiMatch(match -> match
                            .fields("videoName", "tags")
                            .query(keyword)));

            if (Boolean.TRUE.equals(highlight)) {
                searchBuilder.highlight(highlightBuilder -> highlightBuilder
                        .preTags("<span class='highlight'>")
                        .postTags("</span>")
                        .fields("videoName", field -> field));
            }

            SearchOrderTypeEnum orderTypeEnum = SearchOrderTypeEnum.getEnum(orderType);
            if (orderTypeEnum == null) {
                searchBuilder.sort(sort -> sort.score(score -> score.order(SortOrder.Desc)));
            } else {
                searchBuilder.sort(sort -> sort
                        .field(field -> field
                                .field(orderTypeEnum.getField())
                                .order(SortOrder.Desc)));
            }

            SearchResponse<VideoInfoEsDTO> response = elasticsearchClient.search(searchBuilder.build(), VideoInfoEsDTO.class);
            List<VideoSearchResultVO> resultList = response.hits().hits().stream()
                    .map(hit -> toSearchResult(hit, Boolean.TRUE.equals(highlight)))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            fillUserInfo(resultList);
            mergeRedisActionDelta(resultList);

            long totalCount = response.hits().total() == null ? 0 : response.hits().total().value();
            SimplePage simplePage = new SimplePage(realPageNo, (int) totalCount, realPageSize);
            return new PaginationResultVO<>((int) totalCount, simplePage.getPageSize(),
                    simplePage.getPageNo(), simplePage.getPageTotal(), resultList);
        } catch (IOException e) {
            log.error("Elasticsearch 搜索执行失败, keyword={}", keyword, e);
            throw new BusinessException("搜索服务异常，请稍后再试");
        }
    }

    @Override
    public void rebuildVideoIndex() {
        int pageNo = 1;
        while (true) {
            PaginationResultVO<VideoInfoDTO> page = videoInfoClient.loadVideoIndexSource(pageNo, REBUILD_PAGE_SIZE);
            if (page == null || page.getList() == null || page.getList().isEmpty()) {
                return;
            }
            for (VideoInfoDTO videoInfoDTO : page.getList()) {
                saveDoc(videoInfoDTO);
            }
            if (page.getPageTotal() == null || pageNo >= page.getPageTotal()) {
                return;
            }
            pageNo++;
        }
    }

    private VideoSearchResultVO toSearchResult(Hit<VideoInfoEsDTO> hit, boolean highlight) {
        VideoInfoEsDTO videoInfoEsDTO = hit.source();
        if (videoInfoEsDTO == null) {
            return null;
        }
        VideoSearchResultVO resultVO = BeanUtil.toBean(videoInfoEsDTO, VideoSearchResultVO.class);
        if (highlight && hit.highlight().containsKey("videoName") && !hit.highlight().get("videoName").isEmpty()) {
            resultVO.setVideoName(hit.highlight().get("videoName").get(0));
        }
        return resultVO;
    }

    private void fillUserInfo(List<VideoSearchResultVO> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return;
        }
        List<String> userIds = resultList.stream()
                .map(VideoSearchResultVO::getUserId)
                .filter(userId -> !StringTools.isEmpty(userId))
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }

        List<UserInfoDTO> userInfoList;
        try {
            userInfoList = userInfoClient.getUserInfoByIds(userIds);
        } catch (Exception e) {
            // 昵称头像只是展示增强，user 服务短暂不可用时搜索结果仍然可以返回。
            log.warn("批量查询搜索结果用户信息失败, userIdCount={}", userIds.size(), e);
            return;
        }
        if (userInfoList == null || userInfoList.isEmpty()) {
            return;
        }

        Map<String, UserInfoDTO> userInfoMap = userInfoList.stream()
                .filter(userInfo -> !StringTools.isEmpty(userInfo.getUserId()))
                .collect(Collectors.toMap(UserInfoDTO::getUserId, userInfo -> userInfo, (left, right) -> left));

        for (VideoSearchResultVO resultVO : resultList) {
            UserInfoDTO userInfoDTO = userInfoMap.get(resultVO.getUserId());
            if (userInfoDTO == null) {
                continue;
            }
            resultVO.setNickName(userInfoDTO.getNickName());
            resultVO.setAvatar(userInfoDTO.getAvatar());
        }
    }

    private void mergeRedisActionDelta(List<VideoSearchResultVO> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return;
        }
        for (VideoSearchResultVO resultVO : resultList) {
            Map<String, Integer> deltaMap = redisComponent.getVideoActionCountDelta(resultVO.getVideoId());
            if (deltaMap == null || deltaMap.isEmpty()) {
                continue;
            }
            resultVO.setCollectCount(nonNegative(defaultValue(resultVO.getCollectCount())
                    + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COLLECT.getField(), 0)));
            resultVO.setDanmuCount(nonNegative(defaultValue(resultVO.getDanmuCount())
                    + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_DNAMU.getField(), 0)));
        }
    }

    private VideoInfoEsDTO toEsDTO(VideoInfoDTO videoInfo) {
        VideoInfoEsDTO videoInfoEsDTO = BeanUtil.toBean(videoInfo, VideoInfoEsDTO.class);
        videoInfoEsDTO.setPlayCount(defaultValue(videoInfoEsDTO.getPlayCount()));
        videoInfoEsDTO.setDanmuCount(defaultValue(videoInfoEsDTO.getDanmuCount()));
        videoInfoEsDTO.setCollectCount(defaultValue(videoInfoEsDTO.getCollectCount()));
        return videoInfoEsDTO;
    }

    private Map<String, Object> buildUpdateMap(VideoInfoEsDTO videoInfoEsDTO) {
        Map<String, Object> updateMap = new HashMap<>(8);
        putIfNotBlank(updateMap, "videoName", videoInfoEsDTO.getVideoName());
        putIfNotBlank(updateMap, "videoCover", videoInfoEsDTO.getVideoCover());
        putIfNotBlank(updateMap, "userId", videoInfoEsDTO.getUserId());
        putIfNotBlank(updateMap, "tags", videoInfoEsDTO.getTags());
        putIfNotNull(updateMap, "createTime", videoInfoEsDTO.getCreateTime());
        putIfNotNull(updateMap, "playCount", videoInfoEsDTO.getPlayCount());
        putIfNotNull(updateMap, "danmuCount", videoInfoEsDTO.getDanmuCount());
        putIfNotNull(updateMap, "collectCount", videoInfoEsDTO.getCollectCount());
        return updateMap;
    }

    private boolean isExistKey(String id) throws IOException {
        return elasticsearchClient.get(get -> get
                        .index(adminConfig.getEsIndexVideoName())
                        .id(id),
                VideoInfoEsDTO.class).found();
    }

    private void checkVideoId(VideoInfoDTO videoInfo) {
        if (videoInfo == null || StringTools.isEmpty(videoInfo.getVideoId())) {
            throw new BusinessException("视频id不能为空");
        }
    }

    private void putIfNotBlank(Map<String, Object> updateMap, String field, String value) {
        if (!StringTools.isEmpty(value)) {
            updateMap.put(field, value);
        }
    }

    private void putIfNotNull(Map<String, Object> updateMap, String field, Object value) {
        if (value != null) {
            updateMap.put(field, value);
        }
    }

    private int defaultPageNo(Integer pageNo) {
        return pageNo == null || pageNo <= 0 ? 1 : pageNo;
    }

    private int defaultPageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? PageSize.SIZE30.getSize() : pageSize;
    }

    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private int nonNegative(int value) {
        return Math.max(value, 0);
    }
}
