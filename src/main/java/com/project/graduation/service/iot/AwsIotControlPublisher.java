package com.project.graduation.service.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.graduation.config.AwsIotProperties;
import com.project.graduation.domain.plant.Plant;
import com.project.graduation.dto.iot.ControlCommandMqttPayload;
import com.project.graduation.exception.ApiException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aws.iot", name = "enabled", havingValue = "true")
public class AwsIotControlPublisher implements IotControlPublisher {

    private final AwsIotProperties awsIotProperties;
    private final PlantIdResolver plantIdResolver;
    private final ObjectMapper objectMapper;

    private MqttClient mqttClient;

    @Override
    public synchronized void publishCommand(Plant plant, ControlCommandMqttPayload payload) {
        try {
            ensureConnected();
            String topic = buildCommandTopic(plant.getId());
            String json = objectMapper.writeValueAsString(payload);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
            log.info("AWS IoT 제어 명령 publish topic={}, payload={}", topic, json);
        } catch (Exception e) {
            log.error("AWS IoT 제어 명령 publish 실패 plantId={}", plant.getId(), e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "IoT Core 제어 명령 전송에 실패했습니다.");
        }
    }

    private void ensureConnected() throws Exception {
        if (mqttClient != null && mqttClient.isConnected()) {
            return;
        }
        validateCertificatePaths();

        String serverUri = "ssl://" + awsIotProperties.getEndpoint() + ":8883";
        String publisherClientId = awsIotProperties.getClientId() + "-publisher";
        mqttClient = new MqttClient(serverUri, publisherClientId, new MemoryPersistence());

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
        log.info("AWS IoT Core publisher 연결 완료 clientId={}", publisherClientId);
    }

    private String buildCommandTopic(Long plantId) {
        String externalPlantId = plantIdResolver.toExternalId(plantId);
        return "plants/" + externalPlantId + "/" + awsIotProperties.getCommandTopicSuffix();
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            log.warn("AWS IoT publisher 종료 중 오류", e);
        }
    }

    private void validateCertificatePaths() {
        requirePath(awsIotProperties.getRootCaPath(), "aws.iot.root-ca-path");
        requirePath(awsIotProperties.getCertificatePath(), "aws.iot.certificate-path");
        requirePath(awsIotProperties.getPrivateKeyPath(), "aws.iot.private-key-path");
    }

    private void requirePath(String path, String name) {
        if (path == null || path.isBlank()) {
            throw new IllegalStateException(name + " 설정이 필요합니다.");
        }
        if (!java.nio.file.Files.exists(java.nio.file.Path.of(path))) {
            throw new IllegalStateException(name + " 파일을 찾을 수 없습니다: " + path);
        }
    }
}
