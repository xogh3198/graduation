package com.project.graduation.service.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.graduation.config.AwsIotProperties;
import com.project.graduation.dto.iot.PhotoResponseMqttPayload;
import com.project.graduation.iot.AwsIotSslSocketFactory;
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
@ConditionalOnProperty(prefix = "aws.iot", name = "enabled", havingValue = "true")
public class AwsIotPhotoPublisher {

    private final AwsIotProperties awsIotProperties;
    private final ObjectMapper objectMapper;

    private MqttClient mqttClient;

    public synchronized void publishPhotoResponse(String externalPlantId, PhotoResponseMqttPayload payload) {
        try {
            ensureConnected();
            String topic = buildPhotoResponseTopic(externalPlantId);
            String json = objectMapper.writeValueAsString(payload);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
            log.info("AWS IoT photo response publish topic={}, plantId={}", topic, externalPlantId);
        } catch (Exception e) {
            log.error("AWS IoT photo response publish 실패 plantId={}", externalPlantId, e);
        }
    }

    private String buildPhotoResponseTopic(String externalPlantId) {
        return "plants/" + externalPlantId + "/" + awsIotProperties.getPhotoResponseTopicSuffix();
    }

    private void ensureConnected() throws Exception {
        if (mqttClient != null && mqttClient.isConnected()) {
            return;
        }
        validateCertificatePaths();

        String serverUri = "ssl://" + awsIotProperties.getEndpoint() + ":8883";
        String publisherClientId = awsIotProperties.getClientId() + "-photo-publisher";
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
        log.info("AWS IoT Core photo publisher 연결 완료 clientId={}", publisherClientId);
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            log.warn("AWS IoT photo publisher 종료 중 오류", e);
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
