package com.project.graduation.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeathSensorHistoryItem {
    private String timestamp;
    private Double soilMoisture;
    private Double temperature;
    private Double humidity;
    private Double light;
}
