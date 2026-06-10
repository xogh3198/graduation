package com.project.graduation.controller.cam;

import com.project.graduation.dto.cam.KvsTokenResponse;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.cam.KvsTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/camera")
@RequiredArgsConstructor
public class KvsTokenController {

    private final KvsTokenService kvsTokenService;

    /**
     * 라즈베리파이(Master)용 — JWT 없이 등록된 deviceId로 인증.
     */
    @GetMapping("/master-token")
    public ResponseEntity<KvsTokenResponse> getMasterToken(
            @RequestHeader("X-Device-Id") String deviceId) {
        return ResponseEntity.ok(kvsTokenService.issueMasterToken(deviceId));
    }

    /**
     * 프론트엔드(Viewer)용 — 로그인 JWT 필요.
     */
    @GetMapping("/viewer-token")
    public ResponseEntity<KvsTokenResponse> getViewerToken() {
        return ResponseEntity.ok(kvsTokenService.issueViewerToken(AuthUser.getCurrentUserId()));
    }
}
