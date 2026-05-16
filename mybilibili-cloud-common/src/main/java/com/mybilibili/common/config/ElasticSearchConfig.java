package com.mybilibili.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author amani
 * @since 2026.3.31
 */
@Configuration
public class ElasticSearchConfig {

    @Resource
    private AdminConfig adminConfig;
    @Resource
    private Environment environment;

    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        String esHostPort = resolveEsHostPort();
        String[] hostPort = esHostPort.split(":");
        String host = hostPort[0];
        int port = hostPort.length > 1 ? Integer.parseInt(hostPort[1]) : 9200;
        return RestClient.builder(new HttpHost(host, port)).build();
    }

    @Bean(destroyMethod = "close")
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport elasticsearchTransport) {
        return new ElasticsearchClient(elasticsearchTransport);
    }

    /**
     * 优先读取 AI 模块自己的 ES 连接配置，未配置时再回退到公共默认值。
     *
     * <p>这样可以兼容老模块继续使用 `es.host.port`，同时避免 AI 模块把地址写在 `ai.es`
     * 下却始终没有生效，排查时看着配了、实际上没读到。</p>
     */
    private String resolveEsHostPort() {
        String aiEsHostPort = environment.getProperty("ai.es.host-port");
        if (!StringUtils.hasText(aiEsHostPort)) {
            aiEsHostPort = environment.getProperty("ai.es.hostPort");
        }
        if (!StringUtils.hasText(aiEsHostPort)) {
            aiEsHostPort = environment.getProperty("ai.es.host.port");
        }
        if (StringUtils.hasText(aiEsHostPort)) {
            return aiEsHostPort.trim();
        }
        return adminConfig.getEsHostPort();
    }
}
