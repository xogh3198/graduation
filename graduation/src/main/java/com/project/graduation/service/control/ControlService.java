package com.project.graduation.service.control;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.dto.control.ControlResponse;
import com.project.graduation.dto.control.LedControlRequest;
import com.project.graduation.service.plant.PlantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ControlService {

    private final PlantService plantService;

    public ControlResponse water(Long userId, Long plantId, Integer amount) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        publishControl(plant, "water", amount);
        return new ControlResponse("success", amount + "ml 급수 명령을 전송했습니다.");
    }

    public ControlResponse wind(Long userId, Long plantId, Integer duration) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        publishControl(plant, "wind", duration);
        return new ControlResponse("success", duration + "초 바람 제어 명령을 전송했습니다.");
    }

    public ControlResponse led(Long userId, Long plantId, LedControlRequest request) {
        Plant plant = plantService.getOwnedPlant(userId, plantId);
        publishControl(plant, "led", request.getStatus());
        String message = "ON".equals(request.getStatus()) ? "LED가 켜졌습니다." : "LED가 꺼졌습니다.";
        return new ControlResponse("success", message);
    }

    private void publishControl(Plant plant, String command, Object payload) {
        String deviceId = plant.getDeviceId() != null ? plant.getDeviceId() : "unknown";
        log.info("MQTT 제어 명령 전송 - deviceId: {}, command: {}, payload: {}", deviceId, command, payload);
    }
}
