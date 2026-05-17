package com.project.graduation.dto.control;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WindControlRequest {

    @NotNull
    @Positive
    private Integer duration;
}
