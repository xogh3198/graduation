package com.project.graduation.dto.plant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePlantResponse {
    private Long plantId;
    private String status;
    /** 등록한 라즈베리 deviceId */
    private String deviceId;
    /** 사진 MQTT·AI용 (예: plant-1) */
    private String externalPlantId;
}
