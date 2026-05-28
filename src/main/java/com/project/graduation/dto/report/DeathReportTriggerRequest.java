package com.project.graduation.dto.report;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeathReportTriggerRequest {

    @NotBlank
    private String imageUrl;

    private String trigger = "user_button";
}
