package com.mybilibili.search.component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * @author amani
 * @since 2026.3.31
 * 负责通用能力：判断索引是否存在、读取模板、创建索引
 */
@Component
public class ElasticSearchIndexHelper {

    @Resource
    private ElasticsearchClient elasticsearchClient;

    public void createIndexIfAbsent(String indexName, String templatePath) {
        try {
            if (isExistIndex(indexName)) {
                return;
            }
            try (Reader reader = loadTemplate(templatePath)) {
                elasticsearchClient.indices().create(request -> request
                        .index(indexName)
                        .withJson(reader));
            }
        } catch (Exception e) {
            throw new RuntimeException(buildCreateIndexErrorMessage(indexName, templatePath), e);
        }
    }

    public Boolean isExistIndex(String indexName) {
        try {
            return elasticsearchClient.indices()
                    .exists(request -> request.index(indexName))
                    .value();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check elasticsearch index: " + indexName, e);
        }
    }

    private Reader loadTemplate(String templatePath) throws IOException {
        InputStream inputStream = new ClassPathResource(templatePath).getInputStream();
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    private String buildCreateIndexErrorMessage(String indexName, String templatePath) {
        return "Failed to initialize elasticsearch index: " + indexName
                + ", template: " + templatePath
                + ". Please check whether Elasticsearch is reachable and the analyzer plugins required by the template are installed.";
    }
}
