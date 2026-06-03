package com.project.graduation.dto.iot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ControlCommandMqttPayload {

    private final String messageType = "command";
    private final String plantId;
    private final String deviceId;
    private final Actuators actuators;

    public ControlCommandMqttPayload(String plantId, String deviceId, Actuators actuators) {
        this.plantId = plantId;
        this.deviceId = deviceId;
        this.actuators = actuators;
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Actuators {
        private final Integer waterMl;
        private final Integer growLedBrightnessPct;

        public Actuators(Integer waterMl, Integer growLedBrightnessPct) {
            this.waterMl = waterMl;
            this.growLedBrightnessPct = growLedBrightnessPct;
        }

        public static Actuators water(int ml) {
            return new Actuators(ml, null);
        }

        public static Actuators led(int brightnessPct) {
            return new Actuators(null, brightnessPct);
        }
    }
}
