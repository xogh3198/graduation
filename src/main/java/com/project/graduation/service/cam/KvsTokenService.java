package com.project.graduation.service.cam;

import com.project.graduation.config.AwsKvsProperties;
import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.dto.cam.KvsTokenResponse;
import com.project.graduation.exception.ApiException;
import com.project.graduation.service.plant.PlantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.StsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KvsTokenService {

    private final AwsKvsProperties awsKvsProperties;
    private final ObjectProvider<StsClient> stsClientProvider;
    private final PlantRepository plantRepository;
    private final PlantService plantService;

    public KvsTokenResponse issueMasterToken(String deviceId) {
        requireEnabled();
        Plant plant = plantRepository.findByDeviceId(requireDeviceId(deviceId))
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "등록되지 않은 deviceId입니다."));
        log.info("KVS Master 토큰 발급 deviceId={}, plantId={}", deviceId, plant.getId());
        return assumeRole(
                awsKvsProperties.getMasterRoleArn(),
                "PiMasterSession-" + deviceId,
                awsKvsProperties.getMasterDurationSeconds(),
                "MASTER"
        );
    }

    public KvsTokenResponse issueMasterTokenForPlant(String plantRef, String deviceId) {
        requireEnabled();
        String normalizedDeviceId = requireDeviceId(deviceId);
        Plant plant = plantService.getOwnedPlantByRef(plantRef, normalizedDeviceId);
        return issueMasterTokenForPlant(plant);
    }

    public KvsTokenResponse issueMasterTokenForPlant(Plant plant) {
        requireEnabled();
        if (plant.getDeviceId() == null || plant.getDeviceId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "deviceId가 등록되지 않은 식물입니다.");
        }
        log.info("KVS Master 토큰 발급 plantId={}, deviceId={}", plant.getId(), plant.getDeviceId());
        return assumeRole(
                awsKvsProperties.getMasterRoleArn(),
                "PiMasterSession-" + plant.getDeviceId(),
                awsKvsProperties.getMasterDurationSeconds(),
                "MASTER"
        );
    }

    public KvsTokenResponse issueViewerToken(Long userId, Long plantId) {
        requireEnabled();
        plantService.getOwnedPlant(userId, plantId);
        log.info("KVS Viewer 토큰 발급 userId={}, plantId={}", userId, plantId);
        return assumeRole(
                awsKvsProperties.getViewerRoleArn(),
                "WebViewerSession-" + userId + "-" + plantId,
                awsKvsProperties.getViewerDurationSeconds(),
                "VIEWER"
        );
    }

    public KvsTokenResponse issueViewerToken(Long userId) {
        requireEnabled();
        log.info("KVS Viewer 토큰 발급 userId={}", userId);
        return assumeRole(
                awsKvsProperties.getViewerRoleArn(),
                "WebViewerSession-" + userId,
                awsKvsProperties.getViewerDurationSeconds(),
                "VIEWER"
        );
    }

    private static final int STS_MAX_SESSION_SECONDS = 3600;

    private KvsTokenResponse assumeRole(String roleArn, String sessionName, int durationSeconds, String role) {
        validateRoleArn(roleArn, role);
        int effectiveDuration = clampDurationSeconds(durationSeconds);
        StsClient stsClient = stsClientProvider.getIfAvailable();
        if (stsClient == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "KVS STS 클라이언트를 사용할 수 없습니다.");
        }

        try {
            Credentials credentials = stsClient.assumeRole(AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(sessionName)
                    .durationSeconds(effectiveDuration)
                    .build()).credentials();

            return new KvsTokenResponse(
                    credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken(),
                    awsKvsProperties.getRegion(),
                    awsKvsProperties.getChannelName(),
                    credentials.expiration().toString(),
                    role
            );
        } catch (StsException e) {
            log.error("KVS STS AssumeRole 실패 role={}, session={}", roleArn, sessionName, e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "KVS 임시 자격 증명 발급에 실패했습니다.");
        }
    }

    private void requireEnabled() {
        if (!awsKvsProperties.isEnabled()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "KVS 기능이 비활성화되어 있습니다.");
        }
    }

    private void validateRoleArn(String roleArn, String role) {
        if (roleArn == null || roleArn.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "KVS " + role + " Role ARN이 설정되지 않았습니다.");
        }
    }

    private String requireDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "X-Device-Id 헤더가 필요합니다.");
        }
        return deviceId.trim();
    }

    private int clampDurationSeconds(int durationSeconds) {
        if (durationSeconds > STS_MAX_SESSION_SECONDS) {
            log.warn("KVS 토큰 duration {}s 가 IAM Role 최대 {}s 초과 — {}s 로 조정합니다. "
                            + "(12시간이 필요하면 IAM Role MaxSessionDuration을 늘리세요.)",
                    durationSeconds, STS_MAX_SESSION_SECONDS, STS_MAX_SESSION_SECONDS);
            return STS_MAX_SESSION_SECONDS;
        }
        return durationSeconds;
    }
}
