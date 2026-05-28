package com.project.graduation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fcm")
public class FcmProperties {
    private boolean enabled = false;
    private String serverKey = "";
}
