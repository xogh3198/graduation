package com.project.graduation.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DeathReportResponse {
    private Long plantId;
    private LocalDateTime deathDate;
    private String reason;
    private String description;
    private String tips;
}
