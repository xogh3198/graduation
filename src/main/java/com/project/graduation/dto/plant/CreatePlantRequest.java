package com.project.graduation.dto.plant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatePlantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String species;

    @NotNull
    private Integer age;

    private Integer level = 1;

    /** 라즈베리 MQTT 센서 토픽용 (예: pi-001 → device/sensor/pi-001) */
    @NotBlank
    private String deviceId;
}
