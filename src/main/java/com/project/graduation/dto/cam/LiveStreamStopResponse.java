package com.project.graduation.dto.cam;

import lombok.Getter;

@Getter
public class LiveStreamStopResponse {

    private final String status;
    private final String message;

    public LiveStreamStopResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
