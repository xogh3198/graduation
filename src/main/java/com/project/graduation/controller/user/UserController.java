package com.project.graduation.controller.user;

import com.project.graduation.dto.common.StatusResponse;
import com.project.graduation.dto.notification.NotificationResponse;
import com.project.graduation.dto.plant.PlantSummaryResponse;
import com.project.graduation.dto.user.FcmTokenRequest;
import com.project.graduation.dto.user.UserMeResponse;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.plant.PlantService;
import com.project.graduation.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PlantService plantService;

    @PostMapping("/me/fcm-token")
    public ResponseEntity<StatusResponse> updateFcmToken(@Valid @RequestBody FcmTokenRequest request) {
        userService.updateFcmToken(AuthUser.getCurrentUserId(), request.getFcmToken());
        return ResponseEntity.ok(new StatusResponse("success"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMe() {
        return ResponseEntity.ok(userService.getMe(AuthUser.getCurrentUserId()));
    }

    @GetMapping("/me/plants")
    public ResponseEntity<List<PlantSummaryResponse>> getMyPlants() {
        return ResponseEntity.ok(plantService.getMyPlants(AuthUser.getCurrentUserId()));
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(userService.getMyNotifications(AuthUser.getCurrentUserId()));
    }

    @GetMapping("/me/fcm-test")
    public ResponseEntity<Map<String, String>> testFcm() {
        userService.testFcm(AuthUser.getCurrentUserId());
        return ResponseEntity.ok(Map.of("status", "FCM 테스트 요청 성공"));
    }

    @GetMapping("/me/sensor-test")
    public ResponseEntity<Map<String, String>> testSensorAlert() {
        userService.testSensorAlert(AuthUser.getCurrentUserId());
        return ResponseEntity.ok(Map.of("status", "가상 센서(흙 마름) 경고 발생 테스트 성공"));
    }
}
