package com.project.graduation.service.report;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.report.DeathReport;
import com.project.graduation.domain.report.DeathReportRepository;
import com.project.graduation.dto.report.DeathReportResponse;
import com.project.graduation.dto.report.DeathReportTriggerRequest;
import com.project.graduation.exception.ApiException;
import com.project.graduation.service.plant.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DeathReportRepository deathReportRepository;
    private final PlantService plantService;
    private final DeathAnalysisService deathAnalysisService;

    public DeathReportResponse getDeathReport(Long userId, Long plantId) {
        plantService.getOwnedPlant(userId, plantId);

        DeathReport report = deathReportRepository.findByPlantId(plantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사망 리포트가 없습니다."));

        return new DeathReportResponse(
                report.getPlantId(),
                report.getDeathDate(),
                report.getReason(),
                report.getDescription(),
                report.getTips()
        );
    }

    @Transactional
    public DeathReportResponse requestDeathAnalysis(
            Long userId, Long plantId, DeathReportTriggerRequest request) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        String trigger = request.getTrigger() != null ? request.getTrigger() : "user_button";
        return deathAnalysisService.analyzeAndSave(plant, request.getImageUrl(), trigger);
    }
}
