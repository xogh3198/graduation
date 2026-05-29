package com.project.graduation.service.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.project.graduation.config.FcmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmProperties fcmProperties;

    public void send(String fcmToken, String title, String body) {
        if (!fcmProperties.isEnabled()) {
            log.debug("FCM 비활성화 — 푸시 생략 title={}", title);
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase 초기화되지 않음 — 푸시 생략 title={}", title);
            return;
        }
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("FCM 토큰 없음 — 푸시 생략");
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM 푸시 전송 완료 title={}, messageId={}", title, messageId);
        } catch (FirebaseMessagingException e) {
            log.warn("❌ FCM 푸시 전송 실패 title={}, error={}", title, e.getMessage(), e);
        }
    }
}
