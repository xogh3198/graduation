package com.project.graduation.service.user;

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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        
        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "등록된 FCM 토큰이 없습니다. 프론트엔드에서 알림 허용을 먼저 해주세요.");
        }
        
        // 프론트엔드의 알림 이력에서도 볼 수 있도록 DB에 저장
        com.project.graduation.domain.notification.NotificationHistory noti = new com.project.graduation.domain.notification.NotificationHistory();
        noti.setUserId(userId);
        noti.setPlantName("테스트 식물");
        noti.setMessage("테스트 알림이 성공적으로 도착했습니다!");
        noti.setType("info");
        noti.setIsRead(false);
        notificationHistoryRepository.save(noti);

        fcmPushService.send(user.getFcmToken(), "알림 테스트 🔔", "테스트 알림이 성공적으로 도착했습니다!");
    }
}
