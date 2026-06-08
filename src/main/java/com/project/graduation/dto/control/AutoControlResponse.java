package com.project.graduation.dto.control;

import lombok.Getter;

@Getter
public class AutoControlResponse {

    private final String status;
    private final String message;
    private final boolean enabled;
    private final double threshold;

    public AutoControlResponse(String status, String message, boolean enabled, double threshold) {
        this.status = status;
        this.message = message;
        this.enabled = enabled;
        this.threshold = threshold;
    }
}
