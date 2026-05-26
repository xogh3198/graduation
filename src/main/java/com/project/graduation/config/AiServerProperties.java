package com.project.graduation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.server")
public class AiServerProperties {

    private String baseUrl = "http://100.95.251.67:30800";
    private String visionPath = "/ai/v1/analyze/vision";
    private String deathPath = "/ai/v1/analyze/death";
}
