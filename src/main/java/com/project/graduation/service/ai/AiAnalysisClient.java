package com.project.graduation.service.ai;

import com.project.graduation.config.AiServerProperties;
import com.project.graduation.dto.ai.DeathAnalyzeRequest;
import com.project.graduation.dto.ai.DeathAnalyzeResponse;
import com.project.graduation.dto.ai.DeathSensorHistoryItem;
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

import java.util.List;

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

    public DeathAnalyzeResponse analyzeDeath(
            String plantId,
            String imageUrl,
            List<DeathSensorHistoryItem> sensorHistory,
            String trigger) {

        String url = aiServerProperties.getBaseUrl() + aiServerProperties.getDeathPath();
        DeathAnalyzeRequest request = new DeathAnalyzeRequest(plantId, imageUrl, sensorHistory, trigger);

        try {
            DeathAnalyzeResponse response = aiRestClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(DeathAnalyzeResponse.class);

            if (response == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI 서버 death 응답이 비어 있습니다.");
            }
            log.info("AI death 분석 완료 plantId={}, reason={}", plantId, response.getReason());
            return response;
        } catch (RestClientException e) {
            log.error("AI death 요청 실패 url={}, plantId={}", url, plantId, e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI 서버 death 분석 요청에 실패했습니다.");
        }
    }
}
