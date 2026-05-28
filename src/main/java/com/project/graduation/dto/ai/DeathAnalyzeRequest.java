package com.project.graduation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DeathAnalyzeRequest {
    private String plantId;
    private String imageUrl;
    private List<DeathSensorHistoryItem> sensorHistory;
    private String trigger;
}
