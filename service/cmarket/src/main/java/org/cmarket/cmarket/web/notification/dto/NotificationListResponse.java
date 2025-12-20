package org.cmarket.cmarket.web.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;

import java.time.LocalDateTime;

/**
 * 알림 목록 응답 DTO
 * 
 * 알림 목록 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class NotificationListResponse {
    private Long notificationId;
    private String notificationType;
    private String title;
    private String content;
    private String relatedEntityType;
    private Long relatedEntityId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto NotificationDto
     * @return NotificationListResponse
     */
    public static NotificationListResponse fromDto(NotificationDto dto) {
        NotificationListResponse response = new NotificationListResponse();
        response.notificationId = dto.getNotificationId();
        response.notificationType = dto.getNotificationType().name();
        response.title = dto.getTitle();
        response.content = dto.getContent();
        response.relatedEntityType = dto.getRelatedEntityType();
        response.relatedEntityId = dto.getRelatedEntityId();
        response.isRead = dto.getIsRead();
        response.readAt = dto.getReadAt();
        response.createdAt = dto.getCreatedAt();
        return response;
    }
}
