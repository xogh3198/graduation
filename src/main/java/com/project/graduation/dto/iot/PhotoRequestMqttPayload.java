package com.project.graduation.dto.iot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhotoRequestMqttPayload {

    private String plantId;
    private String deviceId;
    private String contentType;
    private String fileName;
}
