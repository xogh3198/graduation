package com.project.graduation.dto.iot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponseMqttPayload {

    private String plantId;
    private String uploadUrl;
    private String bucket;
    private String s3Key;
    private String contentType;
    private String imageUrl;
    private Long expiresInSeconds;
    private String expiresAt;
    private String error;
}
