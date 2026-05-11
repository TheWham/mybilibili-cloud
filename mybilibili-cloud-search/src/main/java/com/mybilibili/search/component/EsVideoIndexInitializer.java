package com.mybilibili.search.component;

import com.mybilibili.common.config.AdminConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author amani
 * @since 2026.3.31
 * 启动时初始化
 */
@Component("elasticSearchComponent")
public class EsVideoIndexInitializer {
    private static final String VIDEO_INDEX_TEMPLATE = "elasticsearch/video-index.json";

    @Resource
    private AdminConfig adminConfig;

    @Resource
    private ElasticSearchIndexHelper elasticSearchIndexHelper;

    public void create() {
        elasticSearchIndexHelper.createIndexIfAbsent(adminConfig.getEsIndexVideoName(), VIDEO_INDEX_TEMPLATE);
    }

}
