package com.project.graduation.service.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.graduation.domain.plant.Plant;
import com.project.graduation.dto.iot.ControlCommandMqttPayload;
import com.project.graduation.util.PlantIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aws.iot", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingIotControlPublisher implements IotControlPublisher {

    private final PlantIdResolver plantIdResolver;
    private final ObjectMapper objectMapper;

    @Override
    public void publishCommand(Plant plant, ControlCommandMqttPayload payload) {
        try {
            String topic = "plants/" + plantIdResolver.toExternalId(plant.getId()) + "/command";
            log.info("[MQTT 발신] IoT 비활성 — 명령 로그만 출력 — 토픽: {}, 내용: {}",
                    topic, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("제어 명령 로그 출력 실패 plantId={}", plant.getId(), e);
        }
    }
}
