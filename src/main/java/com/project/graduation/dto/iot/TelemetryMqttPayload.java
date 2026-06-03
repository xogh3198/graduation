package com.project.graduation.dto.iot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryMqttPayload {

    private String messageType;
    private Long readingId;
    private String plantId;
    private String deviceId;
    private String timestamp;
    private Sensors sensors;
    private Actuators actuators;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sensors {
        private Double lux;
        private Integer soilRaw;
        private Double soilVoltage;
        private Double soilMoisturePct;
        private Double temperatureC;
        private Double humidityPct;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Actuators {
        private Integer growLedBrightnessPct;
    }
}
