package com.project.graduation.service.cam;

import com.project.graduation.config.AwsKvsProperties;
import com.project.graduation.dto.cam.CaptureResponse;
import com.project.graduation.dto.cam.StreamUrlResponse;
import com.project.graduation.service.plant.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CamService {

    private final PlantService plantService;
    private final AwsKvsProperties awsKvsProperties;

    public StreamUrlResponse getStreamUrl(Long userId, Long plantId) {
        plantService.getOwnedPlant(userId, plantId);
        if (awsKvsProperties.isEnabled()) {
            return new StreamUrlResponse(
                    null,
                    awsKvsProperties.getChannelName(),
                    awsKvsProperties.getRegion(),
                    "/api/v1/plants/" + plantId + "/cam/live/start"
            );
        }
        return new StreamUrlResponse(
                "rtsp://localhost:8554/plants/" + plantId + "/live",
                null,
                null,
                null
        );
    }

    public CaptureResponse capture(Long userId, Long plantId) {
        plantService.getOwnedPlant(userId, plantId);
        Long captureId = System.currentTimeMillis();
        String imageUrl = "https://storage.example.com/captures/" + plantId + "/" + UUID.randomUUID() + ".jpg";
        return new CaptureResponse(captureId, imageUrl);
    }
}
