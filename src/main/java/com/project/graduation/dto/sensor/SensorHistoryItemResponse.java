package com.project.graduation.dto.sensor;

import com.project.graduation.domain.sensor.SensorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SensorHistoryItemResponse {
    private LocalDateTime timestamp;
    private String value;
    private SensorType type;
}
