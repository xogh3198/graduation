package com.project.graduation.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VisionAnalyzeRequest {
    private String imageUrl;
    private String plantId;
}
