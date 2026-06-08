package com.project.graduation.dto.control;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AutoControlRequest {

    @NotNull
    private Boolean enabled;

    @NotNull
    @PositiveOrZero
    private Double threshold;
}
