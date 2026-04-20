package com.example.iot.service;

import com.example.iot.domain.SensorData;
import com.example.iot.domain.SensorDataRepository;
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

    // mqttInputChannel로 데이터가 들어오면 이 메서드가 자동 실행됨
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void processMessage(Message<String> message) {
        String payload = message.getPayload();
        // 헤더에서 토픽 이름 추출
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class); 

        log.info("Received message from topic: {} -> payload: {}", topic, payload);

        try {
            // JSON 파싱 (예: {"temperature": 25.5, "humidity": 60.0})
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            SensorData data = new SensorData();
            data.setTopic(topic);
            
            if (jsonNode.has("temperature")) {
                data.setTemperature(jsonNode.get("temperature").asDouble());
            }
            if (jsonNode.has("humidity")) {
                data.setHumidity(jsonNode.get("humidity").asDouble());
            }

            // PostgreSQL에 저장
            repository.save(data);
            log.info("Data saved to database.");

        } catch (Exception e) {
            log.error("Failed to parse and save message", e);
        }
    }
}