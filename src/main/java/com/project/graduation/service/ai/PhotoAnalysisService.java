package com.project.graduation.service.ai;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.plant.PlantStatus;
import com.project.graduation.dto.ai.ImageAnalyzeResponse;
import com.project.graduation.dto.ai.VisionAnalyzeResponse;
import com.project.graduation.dto.iot.PhotoUploadMqttPayload;
import com.project.graduation.exception.ApiException;
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
    private final PlantRepository plantRepository;

    public ImageAnalyzeResponse analyzeImage(String plantId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "imageUrl이 필요합니다.");
        }
        String resolvedPlantId = resolvePlantId(plantId, null);
        VisionAnalyzeResponse vision = aiAnalysisClient.analyzeVision(resolvedPlantId, imageUrl);
        applyVisionResultToPlant(vision);
        return toImageAnalyzeResponse(vision);
    }

    public void handlePhotoUploadEvent(PhotoUploadMqttPayload payload, String topic) {
        String imageUrl = payload.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            log.warn("MQTT photo 이벤트에 imageUrl 없음 topic={}, payload={}", topic, payload);
            return;
        }

        String plantId = resolvePlantId(payload.getPlantId(), topic);
        log.info("S3 업로드 MQTT 수신 plantId={}, imageUrl={}", plantId, imageUrl);

        VisionAnalyzeResponse vision = aiAnalysisClient.analyzeVision(plantId, imageUrl);
        applyVisionResultToPlant(vision);
    }

    private String resolvePlantId(String payloadPlantId, String topic) {
        if (payloadPlantId != null && !payloadPlantId.isBlank()) {
            return payloadPlantId;
        }
        if (topic != null) {
            String[] parts = topic.split("/");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "plantId를 확인할 수 없습니다.");
    }

    @Transactional
    protected void applyVisionResultToPlant(VisionAnalyzeResponse vision) {
        parseNumericPlantId(vision.getPlantId()).ifPresent(plantId -> {
            plantRepository.findById(plantId).ifPresent(plant -> {
                if (vision.getLevel() != null) {
                    plant.setLevel(vision.getLevel());
                }
                plant.setStatus(mapPlantStatus(vision));
                log.info("식물 상태 반영 plantId={}, level={}, status={}",
                        plant.getId(), plant.getLevel(), plant.getStatus());
            });
        });
    }

    private java.util.Optional<Long> parseNumericPlantId(String plantId) {
        if (plantId == null || plantId.isBlank()) {
            return java.util.Optional.empty();
        }
        String numeric = plantId.startsWith("plant-") ? plantId.substring("plant-".length()) : plantId;
        try {
            return java.util.Optional.of(Long.parseLong(numeric));
        } catch (NumberFormatException e) {
            return plantRepository.findAll().stream()
                    .filter(p -> plantId.equals(p.getDeviceId()) || plantId.equals(String.valueOf(p.getId())))
                    .map(Plant::getId)
                    .findFirst();
        }
    }

    private PlantStatus mapPlantStatus(VisionAnalyzeResponse vision) {
        if (Boolean.TRUE.equals(vision.getBug())) {
            return PlantStatus.critical;
        }
        if (vision.getDisease() != null && !vision.getDisease().isBlank() && !"none".equalsIgnoreCase(vision.getDisease())) {
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
