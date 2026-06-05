package com.project.graduation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

    private String bucket;
    private String region = "us-east-1";
    private String keyPrefix = "plants";
    private int presignExpirationMinutes = 15;
}
