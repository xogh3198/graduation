package com.project.graduation.dto.plant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePlantResponse {
    private Long plantId;
    private String status;
}
