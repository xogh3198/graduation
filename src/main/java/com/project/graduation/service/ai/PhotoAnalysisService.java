package com.project.graduation.service.ai;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantStatus;
import com.project.graduation.dto.ai.ImageAnalyzeResponse;
import com.project.graduation.dto.ai.VisionAnalyzeResponse;
import com.project.graduation.dto.iot.PhotoUploadMqttPayload;
import com.project.graduation.exception.ApiException;
import com.project.graduation.service.notification.NotificationService;
import com.project.graduation.service.report.DeathAnalysisService;
import com.project.graduation.service.sensor.SensorSnapshotService;
import com.project.graduation.util.PlantIdResolver;
import com.project.graduation.util.VisionResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoAnalysisService {

    private final AiAnalysisClient aiAnalysisClient;
    private final PlantIdResolver plantIdResolver;
    private final SensorSnapshotService sensorSnapshotService;
    private final DeathAnalysisService deathAnalysisService;
    private final NotificationService notificationService;

    @Transactional
    public ImageAnalyzeResponse analyzeImage(String plantId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "imageUrl이 필요합니다.");
        }
        Plant plant = resolvePlantOrThrow(plantId, null);
        String externalPlantId = plantIdResolver.toExternalId(plant.getId());

        VisionAnalyzeResponse vision = aiAnalysisClient.analyzeVision(externalPlantId, imageUrl);
        vision.setPlantId(externalPlantId);
        applyVisionResult(plant, vision, imageUrl);
        return toImageAnalyzeResponse(vision);
    }

    @Transactional
    public void handlePhotoUploadEvent(PhotoUploadMqttPayload payload, String topic) {
        String imageUrl = payload.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            log.warn("MQTT photo 이벤트에 imageUrl 없음 topic={}, payload={}", topic, payload);
            return;
        }

        Plant plant = resolvePlantOrThrow(payload.getPlantId(), topic);
        String externalPlantId = plantIdResolver.toExternalId(plant.getId());
        log.info("S3 업로드 MQTT 수신 plantId={}, imageUrl={}", externalPlantId, imageUrl);

        VisionAnalyzeResponse vision = aiAnalysisClient.analyzeVision(externalPlantId, imageUrl);
        vision.setPlantId(externalPlantId);
        applyVisionResult(plant, vision, imageUrl);
    }

    private Plant resolvePlantOrThrow(String payloadPlantId, String topic) {
        String plantRef = payloadPlantId;
        if (plantRef == null || plantRef.isBlank()) {
            plantRef = extractPlantRefFromTopic(topic);
        }
        return plantIdResolver.resolvePlant(plantRef)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "plantId를 확인할 수 없습니다. 규칙: plant-{id} 또는 deviceId (예: plant-1, pi-001)"));
    }

    private String extractPlantRefFromTopic(String topic) {
        if (topic == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "plantId를 확인할 수 없습니다.");
        }
        String[] parts = topic.split("/");
        if (parts.length >= 2) {
            return parts[1];
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "MQTT topic에서 plantId를 추출할 수 없습니다.");
    }

    private void applyVisionResult(Plant plant, VisionAnalyzeResponse vision, String imageUrl) {
        if (vision.getLevel() != null) {
            plant.setLevel(vision.getLevel());
        }
        plant.setStatus(mapPlantStatus(vision));

        sensorSnapshotService.applyVisionResult(plant, vision);
        maybeNotify(plant, vision);
        maybeTriggerDeathReport(plant, vision, imageUrl);

        log.info("식물 상태 반영 plantId={}, level={}, status={}",
                plant.getId(), plant.getLevel(), plant.getStatus());
    }

    private void maybeNotify(Plant plant, VisionAnalyzeResponse vision) {
        if (plant.getStatus() == PlantStatus.critical) {
            String message = Boolean.TRUE.equals(vision.getBug())
                    ? plant.getName() + " 식물에서 벌레가 감지되었습니다."
                    : plant.getName() + " 식물 상태가 위험합니다.";
            notificationService.notifyPlantAlert(plant, message, "critical");
            return;
        }
        if (VisionResultMapper.hasActionableDisease(vision.getDisease())) {
            notificationService.notifyPlantAlert(plant,
                    plant.getName() + " 식물에서 병해가 감지되었습니다: " + vision.getDisease(),
                    "critical");
        }
    }

    private void maybeTriggerDeathReport(Plant plant, VisionAnalyzeResponse vision, String imageUrl) {
        if (!shouldTriggerDeathReport(vision)) {
            return;
        }
        deathAnalysisService.analyzeAndSave(plant, imageUrl, "vision_auto");
    }

    private boolean shouldTriggerDeathReport(VisionAnalyzeResponse vision) {
        if (vision.getLevel() != null && vision.getLevel() >= 5) {
            return true;
        }
        if (vision.getStatus() != null && "dead".equalsIgnoreCase(vision.getStatus())) {
            return true;
        }
        return mapPlantStatus(vision) == PlantStatus.dead;
    }

    private PlantStatus mapPlantStatus(VisionAnalyzeResponse vision) {
        if (Boolean.TRUE.equals(vision.getBug())) {
            return PlantStatus.critical;
        }
        if (VisionResultMapper.hasActionableDisease(vision.getDisease())) {
            return PlantStatus.warning;
        }
        if (vision.getStatus() != null) {
            return switch (vision.getStatus().toLowerCase()) {
                case "healthy", "good" -> PlantStatus.good;
                case "warning" -> PlantStatus.warning;
                case "critical", "sick" -> PlantStatus.critical;
                case "dead" -> PlantStatus.dead;
                default -> PlantStatus.good;
            };
        }
        return PlantStatus.good;
    }

    private ImageAnalyzeResponse toImageAnalyzeResponse(VisionAnalyzeResponse vision) {
        String diseaseStatus = vision.getDisease();
        if (diseaseStatus == null || diseaseStatus.isBlank()) {
            diseaseStatus = Boolean.TRUE.equals(vision.getBug()) ? "bug_detected" : "none";
        }
        return new ImageAnalyzeResponse(
                diseaseStatus,
                vision.getLevel(),
                vision.getAccuracy(),
                vision.getStatus(),
                vision.getBug()
        );
    }
}
