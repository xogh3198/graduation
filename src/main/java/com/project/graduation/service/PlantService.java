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

    // 1. 내 식물 목록 조회
    public List<Plant> getPlantsByUserId(Long userId) {
        return plantRepository.findByUserId(userId);
    }

    // 2. 특정 식물 조회
    public Plant getPlantById(Long plantId) {
        return plantRepository.findById(plantId)
                .orElseThrow(() -> new IllegalArgumentException("해당 식물을 찾을 수 없습니다. id=" + plantId));
    }

    // 3. 식물의 최신 센서 데이터 조회 (MVP용 임시 로직)
    public SensorData getLatestSensorData(Long plantId) {
        // 실제로는 plantId와 연동된 deviceId를 바탕으로 DB에서 가장 최신 데이터를 1건 가져와야 합니다.
        // 현재는 에러 방지를 위해 빈 객체를 반환합니다.
        return new SensorData(); 
    }

    // 4. 수치 도서관 히스토리 조회 (MVP용 임시 로직)
    public List<SensorData> getHistory(Long plantId, String startDate, String endDate) {
        // 실제로는 기간별 데이터를 DB에서 조회합니다.
        return List.of();
    }
}