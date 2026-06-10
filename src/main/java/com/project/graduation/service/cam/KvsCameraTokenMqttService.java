package com.project.graduation.service.cam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.graduation.config.AwsIotProperties;
import com.project.graduation.config.AwsKvsProperties;
import com.project.graduation.dto.cam.KvsTokenResponse;
import com.project.graduation.dto.iot.CameraTokenRequestMqttPayload;
import com.project.graduation.iot.AwsIotSslSocketFactory;
import com.project.graduation.util.PlantIdResolver;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aws.kvs", name = "enabled", havingValue = "true")
public class KvsCameraTokenMqttService {

    private final AwsKvsProperties awsKvsProperties;
    private final AwsIotProperties awsIotProperties;
    private final KvsTokenService kvsTokenService;
    private final PlantIdResolver plantIdResolver;
    private final ObjectMapper objectMapper;

    private MqttClient mqttClient;

    public void handleTokenRequest(CameraTokenRequestMqttPayload request, String topic) {
        if (!awsKvsProperties.isEnabled()) {
            log.warn("KVS 비활성 — camera token MQTT 요청 무시 topic={}", topic);
            return;
        }

        String plantRef = request.getPlantId();
        if (plantRef == null || plantRef.isBlank()) {
            plantRef = extractPlantRefFromTopic(topic);
        }

        KvsTokenResponse token = kvsTokenService.issueMasterTokenForPlant(plantRef, request.getDeviceId());
        publishTokenResponse(plantRef, token);
    }

    private void publishTokenResponse(String plantRef, KvsTokenResponse token) {
        try {
            ensureConnected();
            String externalPlantId = plantIdResolver.resolvePlant(plantRef)
                    .map(plant -> plantIdResolver.toExternalId(plant.getId()))
                    .orElse(plantRef);
            String topic = "plants/" + externalPlantId + "/" + awsKvsProperties.getCameraTokenResponseSuffix();
            String json = objectMapper.writeValueAsString(token);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
            log.info("[MQTT 발신] KVS Master 토큰 응답 — 토픽: {}, role={}", topic, token.getRole());
        } catch (Exception e) {
            log.error("KVS Master 토큰 MQTT 응답 실패 plantRef={}", plantRef, e);
        }
    }

    private void ensureConnected() throws Exception {
        if (mqttClient != null && mqttClient.isConnected()) {
            return;
        }

        String serverUri = "ssl://" + awsIotProperties.getEndpoint() + ":8883";
        String clientId = awsIotProperties.getClientId() + "-kvs-publisher";
        mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(AwsIotSslSocketFactory.create(
                awsIotProperties.getRootCaPath(),
                awsIotProperties.getCertificatePath(),
                awsIotProperties.getPrivateKeyPath()
        ));
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        mqttClient.connect(options);
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

    @PreDestroy
    public void shutdown() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            log.warn("KVS MQTT publisher 종료 중 오류", e);
        }
    }
}
