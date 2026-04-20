package com.project.graduation.service;

import com.project.graduation.domain.SensorData;
import com.project.graduation.domain.SensorDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttService {

    private final SensorDataRepository repository;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void processMessage(Message<String> message) {
        String payload = message.getPayload();
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class); 

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            SensorData data = new SensorData();
            data.setTopic(topic);
            
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 3) {
                data.setDeviceId(topicParts[2]); 
            }

            if (jsonNode.has("temperature")) data.setTemperature(jsonNode.get("temperature").asDouble());
            if (jsonNode.has("humidity")) data.setHumidity(jsonNode.get("humidity").asDouble());

            repository.save(data);
            
        } catch (Exception e) {
            log.error("Failed to parse and save message", e);
        }
    }
}