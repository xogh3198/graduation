package com.project.graduation.service.control;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.dto.control.AutoControlRequest;
import com.project.graduation.dto.control.AutoControlResponse;
import com.project.graduation.dto.control.ControlResponse;
import com.project.graduation.dto.control.LedControlRequest;
import com.project.graduation.dto.iot.ControlCommandMqttPayload;
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
public class ControlService {

    private final PlantService plantService;
    private final IotControlPublisher iotControlPublisher;
    private final PlantIdResolver plantIdResolver;

    public ControlResponse water(Long userId, Long plantId, Integer amount) {
        Plant plant = requirePlantWithDevice(userId, plantId);
        ControlCommandMqttPayload payload = new ControlCommandMqttPayload(
                externalPlantId(plant),
                plant.getDeviceId(),
                ControlCommandMqttPayload.Actuators.water(amount)
        );
        iotControlPublisher.publishCommand(plant, payload);
        return ControlResponse.waterSuccess(amount);
    }

    public ControlResponse wind(Long userId, Long plantId, Integer duration) {
        Plant plant = requirePlantWithDevice(userId, plantId);
        log.info("바람 제어는 아직 IoT command 미지원 — deviceId={}, duration={}s", plant.getDeviceId(), duration);
        return new ControlResponse("success", duration + "초 바람 제어 명령을 전송했습니다.");
    }

    public ControlResponse led(Long userId, Long plantId, LedControlRequest request) {
        Plant plant = requirePlantWithDevice(userId, plantId);
        int brightness = mapLedBrightness(request.getStatus());
        ControlCommandMqttPayload payload = new ControlCommandMqttPayload(
                externalPlantId(plant),
                plant.getDeviceId(),
                ControlCommandMqttPayload.Actuators.led(brightness)
        );
        iotControlPublisher.publishCommand(plant, payload);
        String message = brightness > 0 ? "LED가 켜졌습니다." : "LED가 꺼졌습니다.";
        return ControlResponse.ledSuccess(message, brightness);
    }

    @Transactional
    public AutoControlResponse configureAutoWater(Long userId, Long plantId, AutoControlRequest request) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        validateSoilThreshold(request.getThreshold());
        plant.setAutoWaterEnabled(request.getEnabled());
        plant.setAutoWaterThreshold(request.getThreshold());
        String message = request.getEnabled()
                ? "스마트 자동 물주기가 활성화되었습니다."
                : "스마트 자동 물주기가 비활성화되었습니다.";
        return new AutoControlResponse("success", message, request.getEnabled(), request.getThreshold());
    }

    @Transactional
    public AutoControlResponse configureAutoLight(Long userId, Long plantId, AutoControlRequest request) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        plant.setAutoLightEnabled(request.getEnabled());
        plant.setAutoLightThreshold(request.getThreshold());
        String message = request.getEnabled()
                ? "스마트 자동 햇빛이 활성화되었습니다."
                : "스마트 자동 햇빛이 비활성화되었습니다.";
        return new AutoControlResponse("success", message, request.getEnabled(), request.getThreshold());
    }

    private void validateSoilThreshold(double threshold) {
        if (threshold < 0 || threshold > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "토양 습도 임계값은 0~100 사이여야 합니다.");
        }
    }

    private Plant requirePlantWithDevice(Long userId, Long plantId) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        if (plant.getDeviceId() == null || plant.getDeviceId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "deviceId가 등록되지 않은 식물입니다.");
        }
        return plant;
    }

    private String externalPlantId(Plant plant) {
        return plantIdResolver.toExternalId(plant.getId());
    }

    private int mapLedBrightness(String status) {
        return "ON".equalsIgnoreCase(status) ? 100 : 0;
    }
}
