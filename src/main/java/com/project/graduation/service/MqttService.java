package com.project.graduation.service;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

    private final SensorDataRepository repository;
    private final PlantRepository plantRepository;
    private final ObjectMapper objectMapper;

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
                        .ifPresent(plant -> data.setPlantId(plant.getId()));
            }

            repository.save(data);

        } catch (Exception e) {
            log.error("Failed to parse and save message", e);
        }
    }
}
