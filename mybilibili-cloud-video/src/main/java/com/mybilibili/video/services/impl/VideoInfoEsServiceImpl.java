package com.mybilibili.video.services.impl;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.common.utils.StringTools;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.entity.dto.VideoInfoEsDTO;
import com.mybilibili.video.entity.enums.SearchOrderTypeEnum;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.services.VideoEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author amani
 */

@Slf4j
@Service("videoEsService")
public class VideoInfoEsServiceImpl implements VideoEsService {

    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private AdminConfig adminConfig;
    @Resource
    private VideoRedisComponent videoRedisComponent;

    @Override
    public void saveDoc(VideoInfo videoInfo) {
        try {
            if (isExistKey(adminConfig.getEsIndexVideoName(), videoInfo.getVideoId())){
                updateDoc(videoInfo);
            }else {
                VideoInfoEsDTO videoInfoEsDTO = BeanUtil.toBean(videoInfo, VideoInfoEsDTO.class);
                videoInfoEsDTO.setCollectCount(0);
                videoInfoEsDTO.setPlayCount(0);
                videoInfoEsDTO.setDanmuCount(0);

                elasticsearchClient.index(request-> request
                        .index(adminConfig.getEsIndexVideoName())
                        .id(videoInfoEsDTO.getVideoId())
                        .document(videoInfoEsDTO));
            }

        }catch (Exception e) {
            log.error("保存es数据库失败", e);
            throw new BusinessException("保存失败", e);
        }
    }

    @Override
    public void updateDoc(VideoInfo videoInfo) {
        try {
            videoInfo.setLastUpdateTime(null);
            videoInfo.setCreateTime(null);
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            HashMap<String , Object> map = new HashMap<>();
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = videoInfo.getClass().getMethod(methodName);
                Object value = method.invoke(videoInfo);
                if (value != null && value instanceof String && !StringTools.isEmpty(value.toString()) || value != null && !(value instanceof String))
                {
                    map.put(field.getName(), value);
                }
            }
            if (map.isEmpty())
                return;

            elasticsearchClient.update(u -> u
                    .index(adminConfig.getEsIndexVideoName())
                    .id(videoInfo.getVideoId())
                    .doc(map)
                    .upsert(videoInfo),
                    VideoInfo.class
            );

        }catch (Exception e) {
            log.error("更新失败");
            throw new BusinessException("更新失败", e);
        }

    }

    @Override
    public void deleteDoc(String indexName, String videoId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index(indexName)
                    .id(videoId)
            );
        } catch (Exception e){
            log.error("删除失败", e);
            throw new BusinessException("删除es文件失败");

        }
    }

    @Override
    public void updateCount(String indexName, String videoId, Integer changeCount, String field) {
        try {
                    elasticsearchClient.update(u -> u
                            .index(indexName)
                            .id(videoId)
                            .script(s -> s
                                    .inline(inline -> inline
                                            .source("ctx._source." + field + " += params.count")
                                            .params("count", JsonData.of(changeCount)))
                            ),
                    Void.class
            );
        }catch (Exception e) {
            log.error("更新count失败");
            throw new BusinessException("更新数量失败", e);
        }

    }

    @Override
    public PaginationResultVO<VideoInfo> search(Boolean highlight, String keyword, Integer orderType, Integer pageNo, Integer pageSize) {
        try {
            pageNo = pageNo == null ? 1 : pageNo;
            // 1. 构建 SearchRequest 链式调用
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(adminConfig.getEsIndexVideoName())
                    .from((pageNo - 1) * pageSize)
                    .size(pageSize);

            // 2. 构造查询条件 (MultiMatch)
            searchBuilder.query(q -> q
                    .multiMatch(m -> m
                            .fields("videoName", "tags")
                            .query(keyword)
                    )
            );

            // 3. 动态处理高亮
            if (highlight != null && highlight) {
                searchBuilder.highlight(h -> h
                        .preTags("<span class='highlight'>")
                        .postTags("</span>")
                        // 新版 API 只有 fields 方法，通过 lambda 指定具体字段
                        .fields("videoName", f -> f)
                );
            }

            // 4. 动态处理排序
            if (orderType != null) {
                SearchOrderTypeEnum orderEnum = SearchOrderTypeEnum.getEnums(orderType);
                if (orderEnum != null) {
                    searchBuilder.sort(s -> s
                            .field(f -> f
                                    .field(orderEnum.getField())
                                    .order(SortOrder.Desc)
                            )
                    );
                }
            } else {
                // 默认按评分降序
                searchBuilder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            }

            // 5. 执行搜索 (自动将 _source 映射为 VideoInfo 实体)
            SearchResponse<VideoInfo> response = elasticsearchClient.search(searchBuilder.build(), VideoInfo.class);

            // 6. 解析结果集
            List<VideoInfo> videoInfoList = new ArrayList<>();
            for (Hit<VideoInfo> hit : response.hits().hits()) {
                VideoInfo videoInfo = hit.source();
                if (videoInfo == null) continue;

                // 回填高亮内容
                if (highlight != null && highlight && hit.highlight().containsKey("videoName")) {
                    // 获取高亮片段列表的第一个元素
                    String highlightedName = hit.highlight().get("videoName").get(0);
                    videoInfo.setVideoName(highlightedName);
                }

                videoInfoList.add(videoInfo);
            }
            // 微服务拆分后，video 不能直接查 user 表。昵称头像后续由 user 服务接口批量补齐。
            mergeRedisActionDelta(videoInfoList);

            // 获取总条数
            long totalCount = response.hits().total() != null ? response.hits().total().value() : 0;

            SimplePage simplePage = new SimplePage(pageNo, (int)totalCount, pageSize);
            // 7. 封装并返回分页对象
            return new PaginationResultVO<>((int)totalCount, simplePage.getPageSize(), simplePage.getPageNo(), simplePage.getCountTotal(), videoInfoList);

        } catch (IOException e) {
            log.error("Elasticsearch 搜索执行失败, keyword: {}", keyword, e);
            throw new BusinessException("搜索服务异常，请稍后再试");
        }
    }

    private boolean isExistKey(String indexName, String id) throws IOException {

        return elasticsearchClient.get(g -> g
                        .index(indexName)
                        .id(id),
                VideoInfo.class
        ).found();
    }

    private void mergeRedisActionDelta(List<VideoInfo> videoInfoList) {
        if (videoInfoList == null || videoInfoList.isEmpty()) {
            return;
        }
        // 搜索结果来自 ES，刷新时比 MySQL 更容易出现“文档旧、Redis 新”的短暂不一致。
        // 这里把尚未落库的 Redis 增量补进去，前台搜索页和详情页的展示口径就一致了。
        for (VideoInfo videoInfo : videoInfoList) {
            Map<String, Integer> deltaMap = videoRedisComponent.getVideoActionCountDelta(videoInfo.getVideoId());
            if (deltaMap == null || deltaMap.isEmpty()) {
                continue;
            }
            videoInfo.setLikeCount(nonNegative(defaultValue(videoInfo.getLikeCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_LIKE.getField(), 0)));
            videoInfo.setCollectCount(nonNegative(defaultValue(videoInfo.getCollectCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COLLECT.getField(), 0)));
            videoInfo.setCoinCount(nonNegative(defaultValue(videoInfo.getCoinCount()) + deltaMap.getOrDefault(UserActionTypeEnum.VIDEO_COIN.getField(), 0)));
        }
    }

    private int defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private int nonNegative(int value) {
        return Math.max(value, 0);
    }
}
