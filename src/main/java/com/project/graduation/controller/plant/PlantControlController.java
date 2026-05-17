package com.project.graduation.controller.plant;

import com.project.graduation.dto.control.*;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.control.ControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plants/{plantId}/control")
@RequiredArgsConstructor
public class PlantControlController {

    private final ControlService controlService;

    @PostMapping("/water")
    public ResponseEntity<ControlResponse> water(
            @PathVariable Long plantId,
            @Valid @RequestBody WaterControlRequest request) {
        return ResponseEntity.ok(controlService.water(
                AuthUser.getCurrentUserId(), plantId, request.getAmount()));
    }

    @PostMapping("/wind")
    public ResponseEntity<ControlResponse> wind(
            @PathVariable Long plantId,
            @Valid @RequestBody WindControlRequest request) {
        return ResponseEntity.ok(controlService.wind(
                AuthUser.getCurrentUserId(), plantId, request.getDuration()));
    }

    @PostMapping("/led")
    public ResponseEntity<ControlResponse> led(
            @PathVariable Long plantId,
            @Valid @RequestBody LedControlRequest request) {
        return ResponseEntity.ok(controlService.led(
                AuthUser.getCurrentUserId(), plantId, request));
    }
}
