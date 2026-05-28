package com.project.graduation.service.notification;

import com.project.graduation.config.FcmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private static final String FCM_LEGACY_URL = "https://fcm.googleapis.com/fcm/send";

    private final FcmProperties fcmProperties;

    public void send(String fcmToken, String title, String body) {
        if (!fcmProperties.isEnabled() || fcmProperties.getServerKey() == null || fcmProperties.getServerKey().isBlank()) {
            log.debug("FCM 비활성화 또는 server-key 없음 — 푸시 생략 title={}", title);
            return;
        }
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("FCM 토큰 없음 — 푸시 생략");
            return;
        }

        Map<String, Object> payload = Map.of(
                "to", fcmToken,
                "notification", Map.of(
                        "title", title,
                        "body", body
                )
        );

        try {
            RestClient.create().post()
                    .uri(FCM_LEGACY_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "key=" + fcmProperties.getServerKey())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("FCM 푸시 전송 완료 title={}", title);
        } catch (RestClientException e) {
            log.warn("FCM 푸시 전송 실패 title={}", title, e);
        }
    }
}
