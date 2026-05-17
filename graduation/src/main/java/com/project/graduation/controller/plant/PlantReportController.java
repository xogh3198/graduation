package com.project.graduation.controller.plant;

import com.project.graduation.dto.report.DeathReportResponse;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.report.ReportService;
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
}
