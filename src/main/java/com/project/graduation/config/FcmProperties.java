package com.project.graduation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fcm")
public class FcmProperties {
    private boolean enabled = false;
    /** classpath:firebase-service-account.json 또는 file:/path/to/json */
    private String serviceAccountPath = "";
}
