package com.project.graduation.controller.ai;

import com.project.graduation.dto.ai.ImageAnalyzeRequest;
import com.project.graduation.dto.ai.ImageAnalyzeResponse;
import com.project.graduation.service.ai.PhotoAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final PhotoAnalysisService photoAnalysisService;

    @PostMapping("/analyze/image")
    public ResponseEntity<ImageAnalyzeResponse> analyzeImage(@Valid @RequestBody ImageAnalyzeRequest request) {
        ImageAnalyzeResponse response = photoAnalysisService.analyzeImage(
                request.getPlantId(),
                request.getImageUrl()
        );
        return ResponseEntity.ok(response);
    }
}
