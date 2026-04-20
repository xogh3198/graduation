package com.project.graduation.controller;

import com.project.graduation.domain.Plant;
import com.project.graduation.domain.SensorData;
import com.project.graduation.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PlantApiController {

    private final PlantService plantService;

    // 1. 내 식물 목록 조회 (게이미피케이션 메인 화면용)
    @GetMapping("/users/me/plants")
    public ResponseEntity<List<Plant>> getMyPlants(@RequestHeader("Authorization") String token) {
        // 임시로 userId 1L의 식물 반환
        List<Plant> plants = plantService.getPlantsByUserId(1L);
        return ResponseEntity.ok(plants);
    }

    // 2. 식물 현재 상태 통합 조회 (센서 데이터 + 감정 상태)
    @GetMapping("/plants/{plantId}/status")
    public ResponseEntity<?> getPlantStatus(@PathVariable Long plantId) {
        // 가장 최근의 센서 데이터 1건을 DB에서 조회해오는 서비스 로직
        SensorData latestData = plantService.getLatestSensorData(plantId);
        Plant plant = plantService.getPlantById(plantId);
        
        return ResponseEntity.ok(Map.of(
                "temp", latestData.getTemperature(),
                "humidity", latestData.getHumidity(),
                "emotionStatus", plant.getStatus(),
                "level", plant.getLevel()
        ));
    }
    
    // 3. 식물 데이터 히스토리 (수치 도서관)
    @GetMapping("/plants/{plantId}/sensors/history")
    public ResponseEntity<List<SensorData>> getSensorHistory(
            @PathVariable Long plantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        List<SensorData> history = plantService.getHistory(plantId, startDate, endDate);
        return ResponseEntity.ok(history);
    }
}