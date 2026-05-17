package com.project.graduation.dto.plant;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlantSummaryResponse {
    private Long plantId;
    private String name;
    private String species;
    private Integer level;
    private PlantStatus status;

    public static PlantSummaryResponse from(Plant plant) {
        return new PlantSummaryResponse(
                plant.getId(),
                plant.getName(),
                plant.getSpecies(),
                plant.getLevel(),
                plant.getStatus()
        );
    }
}
