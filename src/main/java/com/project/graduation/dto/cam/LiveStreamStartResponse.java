package com.project.graduation.dto.cam;

import lombok.Getter;

@Getter
public class LiveStreamStartResponse {

    private final String status;
    private final String message;
    private final KvsTokenResponse viewer;

    public LiveStreamStartResponse(String status, String message, KvsTokenResponse viewer) {
        this.status = status;
        this.message = message;
        this.viewer = viewer;
    }
}
