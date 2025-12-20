package org.cmarket.cmarket.domain.notification.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.notification.model.Notification;
import org.cmarket.cmarket.domain.notification.model.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 정보 DTO
 * 
 * 앱 서비스에서 사용하는 알림 정보 DTO입니다.
 */
@Getter
@Builder
public class NotificationDto {
    private Long notificationId;
    private NotificationType notificationType;
    private String title;
    private String content;
    private String relatedEntityType;
    private Long relatedEntityId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    
    /**
     * Notification 엔티티를 NotificationDto로 변환
     * 
     * @param notification Notification 엔티티
     * @return NotificationDto
     */
    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
