package com.project.graduation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fcm")
public class FcmProperties {
    private boolean enabled = false;
    /** 서비스 계정 JSON 파일 경로 (classpath: 또는 file: 접두어 가능) */
    private String serviceAccountPath = "";
}
