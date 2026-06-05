package com.project.graduation.service.sensor;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.domain.sensor.SensorType;
import com.project.graduation.dto.sensor.SensorHistoryItemResponse;
import com.project.graduation.dto.sensor.SensorLatestResponse;
import com.project.graduation.dto.sensor.SensorMetricResponse;
import com.project.graduation.service.plant.PlantService;
import com.project.graduation.util.SensorStatusEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorDataRepository sensorDataRepository;
    private final PlantService plantService;

    public SensorLatestResponse getLatest(Long userId, Long plantId) {
        plantService.getOwnedPlant(userId, plantId);

        SensorData latest = sensorDataRepository.findFirstByPlantIdOrderByTimestampDesc(plantId)
                .orElseGet(() -> createDefaultSnapshot(plantId));

        return new SensorLatestResponse(
                metric(latest.getMoisture(), SensorStatusEvaluator.evaluateMoisture(latest.getMoisture())),
                metric(latest.getTemperature(), SensorStatusEvaluator.evaluateTemperature(latest.getTemperature())),
                metric(latest.getLight(), SensorStatusEvaluator.evaluateLight(latest.getLight())),
                metric(resolveSoilMoisture(latest), SensorStatusEvaluator.evaluateSoilMoisture(resolveSoilMoisture(latest))),
                new SensorMetricResponse<>(
                        Boolean.TRUE.equals(latest.getHasBug()),
                        SensorStatusEvaluator.evaluateBug(latest.getHasBug())
                ),
                new SensorMetricResponse<>(
                        latest.getDisease() != null ? latest.getDisease() : "없음",
                        SensorStatusEvaluator.evaluateDisease(latest.getDisease())
                )
        );
    }

    public List<SensorHistoryItemResponse> getHistory(
            Long userId, Long plantId, String startDate, String endDate, String type) {

        Plant plant = plantService.getOwnedPlant(userId, plantId);
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<SensorData> records;
        if (type != null && !type.isBlank()) {
            SensorType sensorType = SensorType.valueOf(type.toLowerCase());
            records = sensorDataRepository.findByPlantIdAndTypeAndTimestampBetweenOrderByTimestampAsc(
                    plant.getId(), sensorType, start, end);
        } else {
            records = sensorDataRepository.findHistory(plant.getId(), start, end);
        }

        return records.stream()
                .map(r -> new SensorHistoryItemResponse(r.getTimestamp(), r.getValue(), r.getType()))
                .toList();
    }

    private Double resolveSoilMoisture(SensorData data) {
        if (data.getSoilMoisture() != null) {
            return data.getSoilMoisture();
        }
        // legacy: soilMoisturePct가 moisture 컬럼에만 저장된 경우
        return data.getMoisture();
    }

    private <T> SensorMetricResponse<T> metric(T value, com.project.graduation.domain.sensor.HealthStatus status) {
        return new SensorMetricResponse<>(value, status);
    }

    private SensorData createDefaultSnapshot(Long plantId) {
        SensorData data = new SensorData();
        data.setPlantId(plantId);
        data.setMoisture(35.0);
        data.setSoilMoisture(35.0);
        data.setTemperature(22.0);
        data.setLight(12000.0);
        data.setHasBug(false);
        data.setDisease("없음");
        data.setTimestamp(LocalDateTime.now());
        return data;
    }
}
