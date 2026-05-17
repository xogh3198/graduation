package com.project.graduation.controller.plant;

import com.project.graduation.dto.cam.CaptureResponse;
import com.project.graduation.dto.cam.StreamUrlResponse;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.cam.CamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plants/{plantId}/cam")
@RequiredArgsConstructor
public class PlantCamController {

    private final CamService camService;

    @GetMapping("/stream-url")
    public ResponseEntity<StreamUrlResponse> getStreamUrl(@PathVariable Long plantId) {
        return ResponseEntity.ok(camService.getStreamUrl(AuthUser.getCurrentUserId(), plantId));
    }

    @PostMapping("/capture")
    public ResponseEntity<CaptureResponse> capture(@PathVariable Long plantId) {
        return ResponseEntity.ok(camService.capture(AuthUser.getCurrentUserId(), plantId));
    }
}
