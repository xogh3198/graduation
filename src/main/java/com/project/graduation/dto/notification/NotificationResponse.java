package com.project.graduation.dto.notification;

import com.project.graduation.domain.notification.NotificationHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private String plantName;
    private String message;
    private String type;
    private LocalDateTime createdAt;
    private Boolean isRead;

    public static NotificationResponse from(NotificationHistory notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getPlantName(),
                notification.getMessage(),
                notification.getType(),
                notification.getCreatedAt(),
                notification.getIsRead()
        );
    }
}
