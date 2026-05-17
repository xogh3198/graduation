package com.project.graduation.service;

import com.project.graduation.domain.Plant;
import com.project.graduation.domain.PlantRepository;
import com.project.graduation.domain.SensorData;
import com.project.graduation.domain.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;
    private final SensorDataRepository sensorDataRepository;

    public List<Plant> getPlantsByUserId(Long userId) {
        return plantRepository.findByUserId(userId);
    }

    public Plant getPlantById(Long plantId) {
        return plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식물을 찾을 수 없습니다. id=" + plantId));
    }

    public SensorData getLatestSensorData(Long plantId) {
        // MVP 임시 반환
        return new SensorData(); 
    }

    public List<SensorData> getHistory(Long plantId, String startDate, String endDate) {
        // MVP 임시 반환
        return List.of();
    }
}