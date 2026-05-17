package com.project.graduation.dto.cam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CaptureResponse {
    private Long captureId;
    private String imageUrl;
}
