package com.project.graduation.dto.control;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LedControlRequest {

    @NotBlank
    @Pattern(regexp = "ON|OFF")
    private String status;
}
