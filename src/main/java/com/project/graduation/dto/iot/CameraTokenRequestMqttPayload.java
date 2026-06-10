package com.project.graduation.dto.iot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CameraTokenRequestMqttPayload {

    private String plantId;
    private String deviceId;
}
