package com.project.graduation.service.notification;

import com.project.graduation.domain.notification.NotificationHistory;
import com.project.graduation.domain.notification.NotificationHistoryRepository;
import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.user.User;
import com.project.graduation.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final UserRepository userRepository;
    private final FcmPushService fcmPushService;

    @Transactional
    public void notifyPlantAlert(Plant plant, String message, String type) {
        NotificationHistory notification = new NotificationHistory();
        notification.setUserId(plant.getUserId());
        notification.setPlantName(plant.getName());
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notificationHistoryRepository.save(notification);

        userRepository.findById(plant.getUserId())
                .map(User::getFcmToken)
                .ifPresent(token -> fcmPushService.send(token, plant.getName(), message));

        log.info("알림 저장 및 FCM 시도 plantId={}, type={}", plant.getId(), type);
    }
}
