package com.project.graduation.dto.sensor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensorLatestResponse {
    private SensorMetricResponse<Double> moisture;
    private SensorMetricResponse<Double> temperature;
    private SensorMetricResponse<Double> light;
    private SensorMetricResponse<Double> soil;
    private SensorMetricResponse<Boolean> bug;
    private SensorMetricResponse<String> disease;
}
