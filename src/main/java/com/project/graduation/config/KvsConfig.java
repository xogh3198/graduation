package com.project.graduation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;

@Configuration
@ConditionalOnProperty(prefix = "aws.kvs", name = "enabled", havingValue = "true")
public class KvsConfig {

    @Bean(destroyMethod = "close")
    public StsClient stsClient(AwsKvsProperties awsKvsProperties) {
        return StsClient.builder()
                .region(Region.of(awsKvsProperties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
