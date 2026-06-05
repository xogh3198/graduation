package com.project.graduation.service.report;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantStatus;
import com.project.graduation.domain.report.DeathReport;
import com.project.graduation.domain.report.DeathReportRepository;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.dto.ai.DeathAnalyzeResponse;
import com.project.graduation.dto.ai.DeathSensorHistoryItem;
import com.project.graduation.dto.report.DeathReportResponse;
import com.project.graduation.service.ai.AiAnalysisClient;
import com.project.graduation.util.PlantIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeathAnalysisService {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    private final AiAnalysisClient aiAnalysisClient;
    private final DeathReportRepository deathReportRepository;
    private final SensorDataRepository sensorDataRepository;
    private final PlantIdResolver plantIdResolver;

    @Transactional
    public DeathReportResponse analyzeAndSave(Plant plant, String imageUrl, String trigger) {
        String externalPlantId = plantIdResolver.toExternalId(plant.getId());
        List<DeathSensorHistoryItem> sensorHistory = buildSensorHistory(plant.getId());

        DeathAnalyzeResponse aiResponse = aiAnalysisClient.analyzeDeath(
                externalPlantId, imageUrl, sensorHistory, trigger);

        DeathReport report = deathReportRepository.findByPlantId(plant.getId())
                .orElse(new DeathReport());
        report.setPlantId(plant.getId());
        report.setDeathDate(LocalDateTime.now());
        report.setReason(aiResponse.getReason());
        report.setDescription(aiResponse.getDescription());
        report.setTips(aiResponse.getTips());
        deathReportRepository.save(report);

        plant.setStatus(PlantStatus.dead);
        if (plant.getLevel() == null || plant.getLevel() < 5) {
            plant.setLevel(5);
        }

        log.info("사망 리포트 저장 plantId={}, trigger={}", plant.getId(), trigger);

        return new DeathReportResponse(
                report.getPlantId(),
                report.getDeathDate(),
                report.getReason(),
                report.getDescription(),
                report.getTips()
        );
    }

    private List<DeathSensorHistoryItem> buildSensorHistory(Long plantId) {
        return sensorDataRepository.findTop30ByPlantIdOrderByTimestampDesc(plantId).stream()
                .map(this::toHistoryItem)
                .toList();
    }

    private DeathSensorHistoryItem toHistoryItem(SensorData data) {
        String timestamp = data.getTimestamp() != null
                ? data.getTimestamp().format(ISO_FORMAT)
                : LocalDateTime.now().format(ISO_FORMAT);
        Double soilMoisture = data.getSoilMoisture() != null ? data.getSoilMoisture() : data.getMoisture();
        return new DeathSensorHistoryItem(
                timestamp,
                soilMoisture,
                data.getTemperature(),
                soilMoisture,
                data.getLight()
        );
    }
}
