package com.mybilibili.ai.component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.mybilibili.ai.config.AiProperties;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * 启动时确保字幕向量索引存在。
 */
@Component
public class AiSubtitleVectorIndexInitializer implements ApplicationRunner {

    private static final String AI_SUBTITLE_VECTOR_INDEX_TEMPLATE = "elasticsearch/video-subtitle-vector-index.json";

    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private AiProperties aiProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createIndexIfAbsent();
    }

    private void createIndexIfAbsent() {
        String indexName = aiProperties.getEs().getSubtitleVectorIndexName();
        try {
            Boolean exists = elasticsearchClient.indices().exists(request -> request.index(indexName)).value();
            if (Boolean.TRUE.equals(exists)) {
                return;
            }
            ClassPathResource resource = new ClassPathResource(AI_SUBTITLE_VECTOR_INDEX_TEMPLATE);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                elasticsearchClient.indices().create(request -> request.index(indexName).withJson(reader));
            }
        } catch (Exception e) {
            throw new IllegalStateException("初始化字幕向量索引失败, index=" + indexName, e);
        }
    }
}
