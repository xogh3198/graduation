package com.project.graduation.service.plant;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.plant.PlantStatus;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.dto.plant.*;
import com.project.graduation.exception.ApiException;
import com.project.graduation.util.PlantIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;
    private final SensorDataRepository sensorDataRepository;

    public List<PlantSummaryResponse> getMyPlants(Long userId) {
        return plantRepository.findByUserId(userId).stream()
                .map(PlantSummaryResponse::from)
                .toList();
    }

    @Transactional
    public CreatePlantResponse createPlant(Long userId, CreatePlantRequest request) {
        String deviceId = request.getDeviceId().trim();
        if (plantRepository.existsByDeviceId(deviceId)) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 등록된 기기 ID입니다: " + deviceId);
        }

        Plant plant = new Plant();
        plant.setUserId(userId);
        plant.setName(request.getName());
        plant.setSpecies(request.getSpecies());
        plant.setAge(request.getAge());
        plant.setLevel(request.getLevel() != null ? request.getLevel() : 1);
        plant.setDeviceId(deviceId);
        plant.setPlantedDate(LocalDate.now());
        plant.setStatus(PlantStatus.good);

        Plant saved = plantRepository.save(plant);
        linkOrphanSensorData(saved.getDeviceId(), saved.getId());

        return new CreatePlantResponse(
                saved.getId(),
                "success",
                saved.getDeviceId(),
                PlantIdResolver.EXTERNAL_PREFIX + saved.getId()
        );
    }

    @Transactional
    public UpdatePlantNameResponse updateName(Long userId, Long plantId, UpdatePlantNameRequest request) {
        Plant plant = getOwnedPlant(userId, plantId);
        plant.setName(request.getName());
        return new UpdatePlantNameResponse(plant.getId(), plant.getName(), "success");
    }

    @Transactional
    public void deletePlant(Long userId, Long plantId) {
        Plant plant = getOwnedPlant(userId, plantId);
        plantRepository.delete(plant);
    }

    public Plant getOwnedPlant(Long userId, Long plantId) {
        return plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "해당 식물을 찾을 수 없습니다."));
    }

    /** 데모·MQTT 선수신 센서 데이터를 새로 등록한 식물에 연결 */
    private void linkOrphanSensorData(String deviceId, Long plantId) {
        List<SensorData> orphanRows = sensorDataRepository.findByDeviceIdAndPlantIdIsNull(deviceId);
        for (SensorData row : orphanRows) {
            row.setPlantId(plantId);
        }
        if (!orphanRows.isEmpty()) {
            log.info("기기 {} 센서 더미 {}건을 plantId={}에 연결", deviceId, orphanRows.size(), plantId);
        }
    }
}
