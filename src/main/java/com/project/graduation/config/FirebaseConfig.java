package com.project.graduation.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    private final FcmProperties fcmProperties;

    @PostConstruct
    public void init() {
        if (!fcmProperties.isEnabled()) {
            log.info("FCM 비활성화 — Firebase 초기화 생략");
            return;
        }

        String path = fcmProperties.getServiceAccountPath();
        if (path == null || path.isBlank()) {
            log.warn("FCM 활성화됐지만 service-account-path가 비어 있음 — Firebase 초기화 생략");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase 이미 초기화됨 — 생략");
            return;
        }

        try {
            Resource resource = new DefaultResourceLoader().getResource(path);
            try (InputStream is = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK 초기화 완료 (path={})", path);
            }
        } catch (IOException e) {
            log.error("❌ Firebase 초기화 실패 — 서비스 계정 JSON 파일을 확인하세요: {}", path, e);
        }
    }
}
