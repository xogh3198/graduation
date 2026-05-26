package com.project.graduation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageAnalyzeResponse {
    private String diseaseStatus;
    private Integer growthStage;
    private Double accuracy;
    private String status;
    private Boolean bug;
}
