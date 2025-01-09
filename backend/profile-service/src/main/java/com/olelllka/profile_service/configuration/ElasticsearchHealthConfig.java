package com.olelllka.profile_service.configuration;

import org.elasticsearch.client.RestClient;
import org.springframework.boot.actuate.elasticsearch.ElasticsearchRestClientHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchHealthConfig {

    @Bean
    public ElasticsearchRestClientHealthIndicator elasticsearchRestClientHealthIndicator(RestClient client) {
        return new ElasticsearchRestClientHealthIndicator(client);
    }

}
