package com.project.graduation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.service.control.PlantAutomationService;
import com.project.graduation.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

    private final SensorDataRepository repository;
    private final PlantRepository plantRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final PlantAutomationService plantAutomationService;

    private final Map<Long, Long> lastAlertTime = new ConcurrentHashMap<>();
    private static final long ALERT_COOLDOWN_MS = 60 * 60 * 1000;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void processMessage(Message<String> message) {
        String payload = message.getPayload();
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        log.info("[MQTT 수신] 토픽: {}, 내용: {}", topic, payload);

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            SensorData data = new SensorData();
            data.setTopic(topic);

            String deviceId = null;
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 3) {
                deviceId = topicParts[2];
                data.setDeviceId(deviceId);
            }

            if (jsonNode.has("temperature")) {
                data.setTemperature(jsonNode.get("temperature").asDouble());
            }
            if (jsonNode.has("humidity")) {
                data.setMoisture(jsonNode.get("humidity").asDouble());
            }
            if (jsonNode.has("moisture")) {
                data.setMoisture(jsonNode.get("moisture").asDouble());
            }
            if (jsonNode.has("light")) {
                data.setLight(jsonNode.get("light").asDouble());
            }
            if (jsonNode.has("soilMoisturePct")) {
                data.setSoilMoisture(jsonNode.get("soilMoisturePct").asDouble());
            }
            if (jsonNode.has("soil")) {
                JsonNode soilNode = jsonNode.get("soil");
                if (soilNode.isNumber()) {
                    data.setSoilMoisture(soilNode.asDouble());
                } else {
                    data.setSoilStatus(soilNode.asText());
                }
            }
            if (jsonNode.has("bug")) {
                data.setHasBug(jsonNode.get("bug").asBoolean());
            }
            if (jsonNode.has("disease")) {
                data.setDisease(jsonNode.get("disease").asText());
            }

            if (deviceId != null) {
                plantRepository.findByDeviceId(deviceId)
                        .ifPresent(plant -> {
                            data.setPlantId(plant.getId());
                            checkSensorAlerts(plant, data);
                            plantAutomationService.applyAutomation(plant, data);
                        });
            }

            repository.save(data);

        } catch (Exception e) {
            log.error("Failed to parse and save message", e);
        }
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
        } else if ("DRY".equalsIgnoreCase(data.getSoilStatus())) {
            msgBuilder.append("흙이 말랐습니다. 물을 주세요! ");
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
            log.info("센서값 위험 알림 발송: {} - {}", plant.getName(), finalMessage);
        }
    }
}
