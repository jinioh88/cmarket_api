package org.cmarket.cmarket.domain.notification.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.notification.model.NotificationType;

/**
 * 알림 생성 명령 DTO
 * 
 * 앱 서비스에서 사용하는 알림 생성 명령 DTO입니다.
 */
@Getter
@Builder
public class NotificationCreateCommand {
    private Long userId;  // 수신자 ID
    private NotificationType notificationType;  // 알림 타입
    private String title;  // 알림 제목
    private String content;  // 알림 내용
    private String relatedEntityType;  // 관련 엔티티 타입 (nullable)
    private Long relatedEntityId;  // 관련 엔티티 ID (nullable)
}
