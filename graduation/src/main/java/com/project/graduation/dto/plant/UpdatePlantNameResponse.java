package com.project.graduation.dto.plant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePlantNameResponse {
    private Long plantId;
    private String name;
    private String status;
}
