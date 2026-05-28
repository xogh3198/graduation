package com.project.graduation.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeathAnalyzeResponse {
    private String plantId;
    private String reason;
    private String description;
    private String tips;
}
