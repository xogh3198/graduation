package com.project.graduation.service.iot;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.dto.iot.TelemetryMqttPayload;
import com.project.graduation.service.control.PlantAutomationService;
import com.project.graduation.service.notification.NotificationService;
import com.project.graduation.util.PlantIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class IotTelemetryService {

    private final SensorDataRepository sensorDataRepository;
    private final PlantIdResolver plantIdResolver;
    private final NotificationService notificationService;
    private final PlantAutomationService plantAutomationService;

    private final Map<Long, Long> lastAlertTime = new ConcurrentHashMap<>();
    private static final long ALERT_COOLDOWN_MS = 60 * 60 * 1000;

    @Transactional
    public void handleTelemetryEvent(TelemetryMqttPayload payload, String topic) {
        SensorData data = new SensorData();
        data.setTopic(topic);

        String plantRef = payload.getPlantId();
        if (plantRef == null || plantRef.isBlank()) {
            plantRef = extractPlantRefFromTopic(topic);
        }
        if (payload.getDeviceId() != null && !payload.getDeviceId().isBlank()) {
            data.setDeviceId(payload.getDeviceId());
        }

        TelemetryMqttPayload.Sensors sensors = payload.getSensors();
        if (sensors != null) {
            if (sensors.getLux() != null) {
                data.setLight(sensors.getLux());
            }
            if (sensors.getSoilMoisturePct() != null) {
                data.setSoilMoisture(sensors.getSoilMoisturePct());
            }
            if (sensors.getTemperatureC() != null) {
                data.setTemperature(sensors.getTemperatureC());
            }
            if (sensors.getHumidityPct() != null) {
                data.setMoisture(sensors.getHumidityPct());
            }
        }

        resolvePlant(plantRef, payload.getDeviceId()).ifPresent(plant -> {
            data.setPlantId(plant.getId());
            if (data.getDeviceId() == null) {
                data.setDeviceId(plant.getDeviceId());
            }
            checkSensorAlerts(plant, data);
            plantAutomationService.applyAutomation(plant, data);
        });

        sensorDataRepository.save(data);
        log.info("IoT telemetry 저장 topic={}, plantId={}, deviceId={}, lux={}, soilMoisturePct={}, humidityPct={}",
                topic, plantRef, data.getDeviceId(), data.getLight(), data.getSoilMoisture(), data.getMoisture());
    }

    private java.util.Optional<Plant> resolvePlant(String plantRef, String deviceId) {
        if (plantRef != null && !plantRef.isBlank()) {
            java.util.Optional<Plant> byPlantRef = plantIdResolver.resolvePlant(plantRef);
            if (byPlantRef.isPresent()) {
                return byPlantRef;
            }
        }
        if (deviceId != null && !deviceId.isBlank()) {
            return plantIdResolver.resolvePlant(deviceId);
        }
        return java.util.Optional.empty();
    }

    private String extractPlantRefFromTopic(String topic) {
        if (topic == null) {
            return null;
        }
        String[] parts = topic.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        return null;
    }

    private void checkSensorAlerts(Plant plant, SensorData data) {
        long now = System.currentTimeMillis();
        Long lastTime = lastAlertTime.get(plant.getId());

        if (lastTime != null && now - lastTime < ALERT_COOLDOWN_MS) {
            return;
        }

        boolean shouldAlert = false;
        StringBuilder msgBuilder = new StringBuilder();

        if (data.getTemperature() != null) {
            if (data.getTemperature() > 35.0) {
                msgBuilder.append("온도가 너무 높습니다(").append(data.getTemperature()).append("°C). ");
                shouldAlert = true;
            } else if (data.getTemperature() < 10.0) {
                msgBuilder.append("온도가 너무 낮습니다(").append(data.getTemperature()).append("°C). ");
                shouldAlert = true;
            }
        }

        if (data.getSoilMoisture() != null && data.getSoilMoisture() < 30.0) {
            msgBuilder.append("흙이 말랐습니다(").append(data.getSoilMoisture()).append("%). ");
            shouldAlert = true;
        }
        if (data.getMoisture() != null && data.getMoisture() < 20.0) {
            msgBuilder.append("대기 습도가 너무 낮습니다(").append(data.getMoisture()).append("%). ");
            shouldAlert = true;
        }

        if (shouldAlert) {
            String finalMessage = msgBuilder.toString().trim();
            notificationService.notifyPlantAlert(plant, finalMessage, "sensor_critical");
            lastAlertTime.put(plant.getId(), now);
            log.info("IoT telemetry 위험 알림: {} - {}", plant.getName(), finalMessage);
        }
    }
}
