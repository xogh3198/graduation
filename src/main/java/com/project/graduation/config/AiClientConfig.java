package com.project.graduation.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({AwsIotProperties.class, AiServerProperties.class})
public class AiClientConfig {

    @Bean
    public RestClient aiRestClient() {
        return RestClient.create();
    }
}
