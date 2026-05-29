package com.project.graduation.service;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.service.notification.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

    private final SensorDataRepository repository;
    private final PlantRepository plantRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    // 알림 도배 방지용 (식물 ID별 마지막 알림 발송 시간 저장)
    private final Map<Long, Long> lastAlertTime = new ConcurrentHashMap<>();
    private static final long ALERT_COOLDOWN_MS = 60 * 60 * 1000; // 1시간 쿨다운

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void processMessage(Message<String> message) {
        String payload = message.getPayload();
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);

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
            if (jsonNode.has("soil")) {
                data.setSoilStatus(jsonNode.get("soil").asText());
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
                            checkSensorAlerts(plant, data); // 센서값 위험 여부 체크 및 알림 발송
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
        
        // 마지막으로 알림을 보낸 지 1시간이 안 지났으면 스킵 (알림 도배 방지)
        if (lastTime != null && now - lastTime < ALERT_COOLDOWN_MS) {
            return;
        }

        boolean shouldAlert = false;
        StringBuilder msgBuilder = new StringBuilder();

        // 1. 온도 체크 (35도 초과 또는 10도 미만)
        if (data.getTemperature() != null) {
            if (data.getTemperature() > 35.0) {
                msgBuilder.append("온도가 너무 높습니다(").append(data.getTemperature()).append("°C). ");
                shouldAlert = true;
            } else if (data.getTemperature() < 10.0) {
                msgBuilder.append("온도가 너무 낮습니다(").append(data.getTemperature()).append("°C). ");
                shouldAlert = true;
            }
        }

        // 2. 습도 체크 (20% 미만)
        if (data.getMoisture() != null && data.getMoisture() < 20.0) {
            msgBuilder.append("대기 습도가 너무 낮습니다(").append(data.getMoisture()).append("%). ");
            shouldAlert = true;
        }

        // 3. 토양 수분 체크 (DRY)
        if ("DRY".equalsIgnoreCase(data.getSoilStatus())) {
            msgBuilder.append("흙이 말랐습니다. 물을 주세요! ");
            shouldAlert = true;
        }

        // 위험 요소가 감지되었다면 알림 발송
        if (shouldAlert) {
            String finalMessage = msgBuilder.toString().trim();
            notificationService.notifyPlantAlert(plant, finalMessage, "sensor_critical");
            lastAlertTime.put(plant.getId(), now); // 마지막 알림 발송 시간 갱신
            log.info("센서값 위험 알림 발송 완료: 식물 {} - {}", plant.getName(), finalMessage);
        }
    }
}
