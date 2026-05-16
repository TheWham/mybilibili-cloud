package com.mybilibili.ai.component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.mybilibili.ai.config.AiProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class AiSubtitleVectorIndexInitializer implements ApplicationRunner {

    private static final String AI_SUBTITLE_VECTOR_INDEX_TEMPLATE = "elasticsearch/video-subtitle-vector-index.json";

    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private AiProperties aiProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!Boolean.TRUE.equals(aiProperties.getEs().getInitEnabled())) {
            log.info("字幕向量索引自动初始化已关闭, index={}", aiProperties.getEs().getSubtitleVectorIndexName());
            return;
        }
        createIndexIfAbsent();
    }

    private void createIndexIfAbsent() {
        String indexName = aiProperties.getEs().getSubtitleVectorIndexName();
        try {
            Boolean exists = elasticsearchClient.indices().exists(request -> request.index(indexName)).value();
            if (Boolean.TRUE.equals(exists)) {
                log.info("字幕向量索引已存在, index={}", indexName);
                return;
            }
            ClassPathResource resource = new ClassPathResource(AI_SUBTITLE_VECTOR_INDEX_TEMPLATE);
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                elasticsearchClient.indices().create(request -> request.index(indexName).withJson(reader));
                log.info("字幕向量索引初始化完成, index={}", indexName);
            }
        } catch (Exception e) {
            if (Boolean.TRUE.equals(aiProperties.getEs().getFailFastOnInitError())) {
                throw new IllegalStateException("初始化字幕向量索引失败, index=" + indexName, e);
            }
            log.error("初始化字幕向量索引失败，但当前配置允许服务继续启动, index={}", indexName, e);
        }
    }
}
