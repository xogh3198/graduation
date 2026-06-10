package com.project.graduation.controller.plant;

import com.project.graduation.dto.cam.CaptureResponse;
import com.project.graduation.dto.cam.KvsTokenResponse;
import com.project.graduation.dto.cam.LiveStreamStartResponse;
import com.project.graduation.dto.cam.LiveStreamStopResponse;
import com.project.graduation.dto.cam.StreamUrlResponse;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.cam.CamService;
import com.project.graduation.service.cam.KvsLiveStreamService;
import com.project.graduation.service.cam.KvsTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plants/{plantId}/cam")
@RequiredArgsConstructor
public class PlantCamController {

    private final CamService camService;
    private final KvsTokenService kvsTokenService;
    private final KvsLiveStreamService kvsLiveStreamService;

    @GetMapping("/stream-url")
    public ResponseEntity<StreamUrlResponse> getStreamUrl(@PathVariable Long plantId) {
        return ResponseEntity.ok(camService.getStreamUrl(AuthUser.getCurrentUserId(), plantId));
    }

    @GetMapping("/viewer-token")
    public ResponseEntity<KvsTokenResponse> getViewerToken(@PathVariable Long plantId) {
        return ResponseEntity.ok(kvsTokenService.issueViewerToken(AuthUser.getCurrentUserId(), plantId));
    }

    @PostMapping("/live/start")
    public ResponseEntity<LiveStreamStartResponse> startLive(@PathVariable Long plantId) {
        return ResponseEntity.ok(kvsLiveStreamService.startLive(AuthUser.getCurrentUserId(), plantId));
    }

    @PostMapping("/live/stop")
    public ResponseEntity<LiveStreamStopResponse> stopLive(@PathVariable Long plantId) {
        return ResponseEntity.ok(kvsLiveStreamService.stopLive(AuthUser.getCurrentUserId(), plantId));
    }

    @PostMapping("/capture")
    public ResponseEntity<CaptureResponse> capture(@PathVariable Long plantId) {
        return ResponseEntity.ok(camService.capture(AuthUser.getCurrentUserId(), plantId));
    }
}
