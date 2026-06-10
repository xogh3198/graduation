package com.project.graduation.service.cam;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.dto.cam.KvsTokenResponse;
import com.project.graduation.dto.cam.LiveStreamStartResponse;
import com.project.graduation.dto.cam.LiveStreamStopResponse;
import com.project.graduation.dto.iot.CameraLiveCommandMqttPayload;
import com.project.graduation.exception.ApiException;
import com.project.graduation.service.iot.IotControlPublisher;
import com.project.graduation.service.plant.PlantService;
import com.project.graduation.util.PlantIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KvsLiveStreamService {

    private final PlantService plantService;
    private final KvsTokenService kvsTokenService;
    private final IotControlPublisher iotControlPublisher;
    private final PlantIdResolver plantIdResolver;

    @Transactional(readOnly = true)
    public LiveStreamStartResponse startLive(Long userId, Long plantId) {
        Plant plant = requirePlantWithDevice(userId, plantId);
        KvsTokenResponse masterToken = kvsTokenService.issueMasterTokenForPlant(plant);
        KvsTokenResponse viewerToken = kvsTokenService.issueViewerToken(userId, plantId);

        String externalPlantId = plantIdResolver.toExternalId(plant.getId());
        CameraLiveCommandMqttPayload command = CameraLiveCommandMqttPayload.start(
                externalPlantId,
                plant.getDeviceId(),
                masterToken
        );
        iotControlPublisher.publishPayload(plant, command);

        log.info("라이브 송출 시작 요청 plantId={}, deviceId={}", plant.getId(), plant.getDeviceId());
        return new LiveStreamStartResponse(
                "success",
                "라이브 송출을 시작했습니다. Viewer 토큰으로 시청하세요.",
                viewerToken
        );
    }

    @Transactional(readOnly = true)
    public LiveStreamStopResponse stopLive(Long userId, Long plantId) {
        Plant plant = requirePlantWithDevice(userId, plantId);

        String externalPlantId = plantIdResolver.toExternalId(plant.getId());
        CameraLiveCommandMqttPayload command = CameraLiveCommandMqttPayload.stop(
                externalPlantId,
                plant.getDeviceId()
        );
        iotControlPublisher.publishPayload(plant, command);

        log.info("라이브 송출 중지 요청 plantId={}, deviceId={}", plant.getId(), plant.getDeviceId());
        return new LiveStreamStopResponse("success", "라이브 송출을 중지했습니다.");
    }

    private Plant requirePlantWithDevice(Long userId, Long plantId) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        if (plant.getDeviceId() == null || plant.getDeviceId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "deviceId가 등록되지 않은 식물입니다.");
        }
        return plant;
    }
}
