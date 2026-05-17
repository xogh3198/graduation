package com.project.graduation.controller.plant;

import com.project.graduation.dto.sensor.SensorHistoryItemResponse;
import com.project.graduation.dto.sensor.SensorLatestResponse;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.sensor.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plants/{plantId}/sensors")
@RequiredArgsConstructor
public class PlantSensorController {

    private final SensorService sensorService;

    @GetMapping("/latest")
    public ResponseEntity<SensorLatestResponse> getLatest(@PathVariable Long plantId) {
        return ResponseEntity.ok(sensorService.getLatest(AuthUser.getCurrentUserId(), plantId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SensorHistoryItemResponse>> getHistory(
            @PathVariable Long plantId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(sensorService.getHistory(
                AuthUser.getCurrentUserId(), plantId, startDate, endDate, type));
    }
}
