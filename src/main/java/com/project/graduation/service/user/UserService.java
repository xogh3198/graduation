package com.project.graduation.service.user;

import com.project.graduation.domain.notification.NotificationHistory;
import com.project.graduation.domain.notification.NotificationHistoryRepository;
import com.project.graduation.domain.user.User;
import com.project.graduation.domain.user.UserRepository;
import com.project.graduation.dto.notification.NotificationResponse;
import com.project.graduation.dto.user.UserMeResponse;
import com.project.graduation.exception.ApiException;
import com.project.graduation.service.notification.FcmPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final FcmPushService fcmPushService;

    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.setFcmToken(fcmToken);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return new UserMeResponse(user.getName());
    }

    @Transactional
    public void testFcm(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "등록된 FCM 토큰이 없습니다. 프론트엔드에서 먼저 토큰을 등록해주세요.");
        }

        // DB에 알림 이력 저장 (테스트용 가짜 데이터)
        NotificationHistory notification = new NotificationHistory();
        notification.setUserId(user.getId());
        notification.setPlantName("테스트 식물");
        notification.setMessage("테스트 알림이 성공적으로 도착했습니다!");
        notification.setType("test");
        notification.setIsRead(false);
        notificationHistoryRepository.save(notification);

        // 실제 푸시 발송
        fcmPushService.send(user.getFcmToken(), "알림 테스트 🔔", "테스트 알림이 성공적으로 도착했습니다!");
    }

    public void testSensorAlert(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "등록된 FCM 토큰이 없습니다.");
        }

        // DB에 센서 경고 이력 저장
        NotificationHistory notification = new NotificationHistory();
        notification.setUserId(user.getId());
        notification.setPlantName("테스트 식물");
        notification.setMessage("흙이 말랐습니다. 물을 주세요! (테스트)");
        notification.setType("sensor_critical");
        notification.setIsRead(false);
        notificationHistoryRepository.save(notification);

        // 푸시 발송
        fcmPushService.send(user.getFcmToken(), "센서 경고 테스트 🚨", "테스트 식물의 흙이 말랐습니다. 물을 주세요!");
    }
}
