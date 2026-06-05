package com.project.graduation.service.iot;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.dto.iot.PhotoRequestMqttPayload;
import com.project.graduation.dto.iot.PhotoResponseMqttPayload;
import com.project.graduation.service.s3.S3PresignService;
import com.project.graduation.util.PlantIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean({S3PresignService.class, AwsIotPhotoPublisher.class})
public class IotPhotoPresignService {

    private final S3PresignService s3PresignService;
    private final AwsIotPhotoPublisher awsIotPhotoPublisher;
    private final PlantIdResolver plantIdResolver;

    public void handlePhotoRequest(PhotoRequestMqttPayload request, String topic) {
        String plantRef = request.getPlantId();
        if (plantRef == null || plantRef.isBlank()) {
            plantRef = extractPlantRefFromTopic(topic);
        }

        if (plantRef == null || plantRef.isBlank()) {
            log.warn("photo/request에 plantId 없음 topic={}, payload={}", topic, request);
            return;
        }

        Plant plant = plantIdResolver.resolvePlant(plantRef).orElse(null);
        if (plant == null) {
            publishError(plantRef, "등록되지 않은 plantId입니다: " + plantRef);
            return;
        }

        String externalPlantId = plantIdResolver.toExternalId(plant.getId());
        if (request.getDeviceId() != null && !request.getDeviceId().isBlank()
                && plant.getDeviceId() != null
                && !plant.getDeviceId().equalsIgnoreCase(request.getDeviceId().trim())) {
            log.warn("photo/request deviceId 불일치 plantId={}, expected={}, actual={}",
                    externalPlantId, plant.getDeviceId(), request.getDeviceId());
        }

        try {
            S3PresignService.PresignedUpload upload =
                    s3PresignService.createPutUpload(externalPlantId, request.getContentType());

            PhotoResponseMqttPayload response = PhotoResponseMqttPayload.builder()
                    .plantId(externalPlantId)
                    .uploadUrl(upload.uploadUrl())
                    .bucket(upload.bucket())
                    .s3Key(upload.s3Key())
                    .contentType(upload.contentType())
                    .imageUrl(upload.imageUrl())
                    .expiresInSeconds(upload.expiresInSeconds())
                    .expiresAt(upload.expiresAt().toString())
                    .build();

            awsIotPhotoPublisher.publishPhotoResponse(externalPlantId, response);
            log.info("photo presign 발급 plantId={}, s3Key={}, expiresInSeconds={}",
                    externalPlantId, upload.s3Key(), upload.expiresInSeconds());
        } catch (Exception e) {
            log.error("photo presign 발급 실패 plantId={}", externalPlantId, e);
            publishError(externalPlantId, "Presigned URL 생성에 실패했습니다.");
        }
    }

    private void publishError(String externalPlantId, String message) {
        PhotoResponseMqttPayload response = PhotoResponseMqttPayload.builder()
                .plantId(externalPlantId)
                .error(message)
                .build();
        awsIotPhotoPublisher.publishPhotoResponse(externalPlantId, response);
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
}
