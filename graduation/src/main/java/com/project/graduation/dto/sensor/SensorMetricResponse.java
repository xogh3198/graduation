package com.project.graduation.dto.sensor;

import com.project.graduation.domain.sensor.HealthStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensorMetricResponse<T> {
    private T value;
    private HealthStatus status;
}
