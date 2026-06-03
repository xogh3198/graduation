package com.project.graduation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws.iot")
public class AwsIotProperties {

    private boolean enabled = false;
    private String endpoint = "amtgq6rjeeepu-ats.iot.us-east-1.amazonaws.com";
    private String clientId = "graduation-backend";
    private String photoTopic = "plants/+/status/photo";
    private String telemetryTopic = "plants/+/telemetry";
    /** publish topic: plants/plant-1/{suffix} */
    private String commandTopicSuffix = "command";
    private String certificatePath;
    private String privateKeyPath;
    private String rootCaPath;
}
