package com.project.graduation.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisionAnalyzeResponse {

    private String plantId;
    private Integer level;
    private Boolean bug;
    private String disease;
    private String status;
    private Double accuracy;
    private String modelMode;
    private List<Detection> detections;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Detection {
        private Double x1;
        private Double y1;
        private Double x2;
        private Double y2;
        private String label;
        private Double confidence;
    }
}
