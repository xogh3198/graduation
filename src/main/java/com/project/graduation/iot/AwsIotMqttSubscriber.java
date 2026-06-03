package com.project.graduation.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.graduation.config.AwsIotProperties;
import com.project.graduation.dto.iot.PhotoUploadMqttPayload;
import com.project.graduation.dto.iot.TelemetryMqttPayload;
import com.project.graduation.service.ai.PhotoAnalysisService;
import com.project.graduation.service.iot.IotTelemetryService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "aws.iot", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AwsIotMqttSubscriber implements MqttCallback {

    private final AwsIotProperties awsIotProperties;
    private final PhotoAnalysisService photoAnalysisService;
    private final IotTelemetryService iotTelemetryService;
    private final ObjectMapper objectMapper;

    private MqttClient mqttClient;

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws Exception {
        validateCertificatePaths();

        String serverUri = "ssl://" + awsIotProperties.getEndpoint() + ":8883";
        mqttClient = new MqttClient(serverUri, awsIotProperties.getClientId(), new MemoryPersistence());
        mqttClient.setCallback(this);

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

        String photoTopic = awsIotProperties.getPhotoTopic();
        String telemetryTopic = awsIotProperties.getTelemetryTopic();

        log.info("AWS IoT Core 연결 시도 endpoint={}, photoTopic={}, telemetryTopic={}",
                awsIotProperties.getEndpoint(), photoTopic, telemetryTopic);
        mqttClient.connect(options);
        mqttClient.subscribe(new String[]{photoTopic, telemetryTopic}, new int[]{1, 1});
        log.info("AWS IoT Core 구독 시작 완료");
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("AWS IoT Core 연결 끊김", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        try {
            if (isTelemetryTopic(topic, payload)) {
                TelemetryMqttPayload event = objectMapper.readValue(payload, TelemetryMqttPayload.class);
                iotTelemetryService.handleTelemetryEvent(event, topic);
                return;
            }

            PhotoUploadMqttPayload event = objectMapper.readValue(payload, PhotoUploadMqttPayload.class);
            photoAnalysisService.handlePhotoUploadEvent(event, topic);
        } catch (Exception e) {
            log.error("AWS IoT MQTT 메시지 처리 실패 topic={}, payload={}", topic, payload, e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // subscriber only
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            log.warn("AWS IoT MQTT 종료 중 오류", e);
        }
    }

    private boolean isTelemetryTopic(String topic, String payload) {
        if (topic != null && topic.endsWith("/telemetry")) {
            return true;
        }
        return payload.contains("\"messageType\":\"telemetry\"")
                || payload.contains("\"messageType\": \"telemetry\"");
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
