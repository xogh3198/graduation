package com.project.graduation.controller.plant;

import com.project.graduation.dto.report.DeathReportResponse;
import com.project.graduation.dto.report.DeathReportTriggerRequest;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plants/{plantId}/reports")
@RequiredArgsConstructor
public class PlantReportController {

    private final ReportService reportService;

    @GetMapping("/death")
    public ResponseEntity<DeathReportResponse> getDeathReport(@PathVariable Long plantId) {
        return ResponseEntity.ok(reportService.getDeathReport(AuthUser.getCurrentUserId(), plantId));
    }

    /** 사용자 "식물이 죽었어요" 버튼 → AI death 분석 후 DeathReport 저장 */
    @PostMapping("/death")
    public ResponseEntity<DeathReportResponse> requestDeathReport(
            @PathVariable Long plantId,
            @Valid @RequestBody DeathReportTriggerRequest request) {
        return ResponseEntity.ok(reportService.requestDeathAnalysis(
                AuthUser.getCurrentUserId(), plantId, request));
    }
}
