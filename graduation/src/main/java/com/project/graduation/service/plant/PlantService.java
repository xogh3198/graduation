package com.project.graduation.service.plant;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.plant.PlantStatus;
import com.project.graduation.dto.plant.*;
import com.project.graduation.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;

    public List<PlantSummaryResponse> getMyPlants(Long userId) {
        return plantRepository.findByUserId(userId).stream()
                .map(PlantSummaryResponse::from)
                .toList();
    }

    @Transactional
    public CreatePlantResponse createPlant(Long userId, CreatePlantRequest request) {
        Plant plant = new Plant();
        plant.setUserId(userId);
        plant.setName(request.getName());
        plant.setSpecies(request.getSpecies());
        plant.setAge(request.getAge());
        plant.setLevel(request.getLevel() != null ? request.getLevel() : 1);
        plant.setPlantedDate(LocalDate.now());
        plant.setStatus(PlantStatus.good);

        Plant saved = plantRepository.save(plant);
        return new CreatePlantResponse(saved.getId(), "success");
    }

    @Transactional
    public UpdatePlantNameResponse updateName(Long userId, Long plantId, UpdatePlantNameRequest request) {
        Plant plant = getOwnedPlant(userId, plantId);
        plant.setName(request.getName());
        return new UpdatePlantNameResponse(plant.getId(), plant.getName(), "success");
    }

    public Plant getOwnedPlant(Long userId, Long plantId) {
        return plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "해당 식물을 찾을 수 없습니다."));
    }
}
