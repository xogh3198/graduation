package com.project.graduation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws.kvs")
public class AwsKvsProperties {

    private boolean enabled = false;
    private String region = "us-east-1";
    private String channelName = "inha-capstone-07-pi-camera-channel";
    private String masterRoleArn = "";
    private String viewerRoleArn = "";
    private int masterDurationSeconds = 43200;
    private int viewerDurationSeconds = 3600;
    private String cameraTokenRequestTopic = "plants/+/camera/token/request";
    private String cameraTokenResponseSuffix = "camera/token/response";
}
