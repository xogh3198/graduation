package com.project.graduation.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImageAnalyzeRequest {

    @NotBlank
    private String imageUrl;

    private String plantId;
}
