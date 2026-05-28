package com.project.graduation.controller.user;

import com.project.graduation.dto.common.StatusResponse;
import com.project.graduation.dto.notification.NotificationResponse;
import com.project.graduation.dto.plant.PlantSummaryResponse;
import com.project.graduation.dto.user.FcmTokenRequest;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.plant.PlantService;
import com.project.graduation.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/me/plants")
    public ResponseEntity<List<PlantSummaryResponse>> getMyPlants() {
        return ResponseEntity.ok(plantService.getMyPlants(AuthUser.getCurrentUserId()));
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(userService.getMyNotifications(AuthUser.getCurrentUserId()));
    }
}
