package com.project.graduation.service.ai;

import com.project.graduation.config.AiServerProperties;
import com.project.graduation.dto.ai.VisionAnalyzeRequest;
import com.project.graduation.dto.ai.VisionAnalyzeResponse;
import com.project.graduation.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisClient {

    private final RestClient aiRestClient;
    private final AiServerProperties aiServerProperties;

    public VisionAnalyzeResponse analyzeVision(String plantId, String imageUrl) {
        String url = aiServerProperties.getBaseUrl() + aiServerProperties.getVisionPath();
        VisionAnalyzeRequest request = new VisionAnalyzeRequest(imageUrl, plantId);

        try {
            VisionAnalyzeResponse response = aiRestClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(VisionAnalyzeResponse.class);

            if (response == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI 서버 응답이 비어 있습니다.");
            }
            log.info("AI vision 분석 완료 plantId={}, level={}, status={}",
                    response.getPlantId(), response.getLevel(), response.getStatus());
            return response;
        } catch (RestClientException e) {
            log.error("AI vision 요청 실패 url={}, plantId={}", url, plantId, e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI 서버 vision 분석 요청에 실패했습니다.");
        }
    }
}
