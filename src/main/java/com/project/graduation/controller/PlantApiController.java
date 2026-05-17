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

    @GetMapping("/users/me/plants")
    public ResponseEntity<List<Plant>> getMyPlants(@RequestHeader(value="Authorization", required=false) String token) {
        List<Plant> plants = plantService.getPlantsByUserId(1L);
        return ResponseEntity.ok(plants);
    }

    @GetMapping("/plants/{plantId}/status")
    public ResponseEntity<?> getPlantStatus(@PathVariable Long plantId) {
        SensorData latestData = plantService.getLatestSensorData(plantId);
        Plant plant = plantService.getPlantById(plantId);
        
        return ResponseEntity.ok(Map.of(
                "temp", latestData.getTemperature(),
                "humidity", latestData.getHumidity(),
                "emotionStatus", plant.getStatus(),
                "level", plant.getLevel()
        ));
    }
    
    @GetMapping("/plants/{plantId}/sensors/history")
    public ResponseEntity<List<SensorData>> getSensorHistory(
            @PathVariable Long plantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        List<SensorData> history = plantService.getHistory(plantId, startDate, endDate);
        return ResponseEntity.ok(history);
    }
}