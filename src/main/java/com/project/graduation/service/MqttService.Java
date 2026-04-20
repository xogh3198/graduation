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
            
            // 토픽 구조가 device/sensor/{deviceId} 라고 가정하고 추출.   c
            String[] topicParts = topic.split("/");
            if (topicParts.length >= 3) {
                data.setDeviceId(topicParts[2]); // 엔티티에 deviceId 필드 추가 필요
            }

            if (jsonNode.has("temperature")) data.setTemperature(jsonNode.get("temperature").asDouble());
            if (jsonNode.has("humidity")) data.setHumidity(jsonNode.get("humidity").asDouble());
            // 추가 센서 (조도, 토양 습도 등) 파싱

            repository.save(data);
            
            // TODO: 데이터 저장 시 이벤트(Event)를 발행하여 식물의 경험치나 상태를 업데이트하는 로직 트리거 가능. 
            
        } catch (Exception e) {
            log.error("Failed to parse and save message", e);
        }
    }
}