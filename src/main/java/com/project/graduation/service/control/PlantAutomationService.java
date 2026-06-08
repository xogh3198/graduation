package com.project.graduation.service.control;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.dto.iot.ControlCommandMqttPayload;
import com.project.graduation.service.iot.IotControlPublisher;
import com.project.graduation.util.PlantIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantAutomationService {

    private static final int AUTO_WATER_AMOUNT_ML = 100;
    private static final long AUTO_WATER_COOLDOWN_MS = 30 * 60 * 1000L;

    private final IotControlPublisher iotControlPublisher;
    private final PlantIdResolver plantIdResolver;

    private final Map<Long, Long> lastAutoWaterTime = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> lastAutoLightOn = new ConcurrentHashMap<>();

    public void applyAutomation(Plant plant, SensorData data) {
        if (plant.getDeviceId() == null || plant.getDeviceId().isBlank()) {
            return;
        }
        applyAutoWater(plant, data);
        applyAutoLight(plant, data);
    }

    private void applyAutoWater(Plant plant, SensorData data) {
        if (!Boolean.TRUE.equals(plant.getAutoWaterEnabled()) || plant.getAutoWaterThreshold() == null) {
            return;
        }
        if (data.getSoilMoisture() == null || data.getSoilMoisture() > plant.getAutoWaterThreshold()) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastTime = lastAutoWaterTime.get(plant.getId());
        if (lastTime != null && now - lastTime < AUTO_WATER_COOLDOWN_MS) {
            return;
        }

        publishWater(plant, AUTO_WATER_AMOUNT_ML);
        lastAutoWaterTime.put(plant.getId(), now);
        log.info("스마트 자동 물주기 실행 plantId={}, soilMoisture={}%, threshold={}%",
                plant.getId(), data.getSoilMoisture(), plant.getAutoWaterThreshold());
    }

    private void applyAutoLight(Plant plant, SensorData data) {
        if (!Boolean.TRUE.equals(plant.getAutoLightEnabled()) || plant.getAutoLightThreshold() == null) {
            return;
        }
        if (data.getLight() == null) {
            return;
        }

        boolean shouldBeOn = data.getLight() <= plant.getAutoLightThreshold();
        Boolean previous = lastAutoLightOn.get(plant.getId());
        if (previous != null && previous == shouldBeOn) {
            return;
        }

        int brightness = shouldBeOn ? 100 : 0;
        publishLed(plant, brightness);
        lastAutoLightOn.put(plant.getId(), shouldBeOn);
        log.info("스마트 자동 조명 실행 plantId={}, lux={}, threshold={}, led={}%",
                plant.getId(), data.getLight(), plant.getAutoLightThreshold(), brightness);
    }

    private void publishWater(Plant plant, int amountMl) {
        ControlCommandMqttPayload payload = new ControlCommandMqttPayload(
                plantIdResolver.toExternalId(plant.getId()),
                plant.getDeviceId(),
                ControlCommandMqttPayload.Actuators.water(amountMl)
        );
        iotControlPublisher.publishCommand(plant, payload);
    }

    private void publishLed(Plant plant, int brightnessPct) {
        ControlCommandMqttPayload payload = new ControlCommandMqttPayload(
                plantIdResolver.toExternalId(plant.getId()),
                plant.getDeviceId(),
                ControlCommandMqttPayload.Actuators.led(brightnessPct)
        );
        iotControlPublisher.publishCommand(plant, payload);
    }
}
