package com.project.graduation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(prefix = "aws.s3", name = "bucket")
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(AwsS3Properties awsS3Properties) {
        return S3Presigner.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
