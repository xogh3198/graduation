package com.project.graduation.dto.cam;

import lombok.Getter;

@Getter
public class KvsTokenResponse {

    private final String accessKeyId;
    private final String secretAccessKey;
    private final String sessionToken;
    private final String region;
    private final String channelName;
    private final String expiration;
    private final String role;

    public KvsTokenResponse(
            String accessKeyId,
            String secretAccessKey,
            String sessionToken,
            String region,
            String channelName,
            String expiration,
            String role) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.sessionToken = sessionToken;
        this.region = region;
        this.channelName = channelName;
        this.expiration = expiration;
        this.role = role;
    }
}
