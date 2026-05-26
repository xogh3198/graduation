package com.project.graduation.dto.iot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhotoUploadMqttPayload {

    private String plantId;
    private String imageUrl;
    private String s3Key;
    private String bucket;
}
