package com.project.graduation.dto.plant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePlantNameRequest {

    @NotBlank
    private String name;
}
