package org.cmarket.cmarket.domain.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 * 
 * 사용자에게 전송되는 알림 정보를 저장하는 도메인 모델입니다.
 * - 읽음/안 읽음 상태 관리
 * - 관련 엔티티 정보 저장 (상세 페이지 이동용)
 * - 다양한 알림 타입 지원
 */
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_is_read", columnList = "is_read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at"),
        @Index(name = "idx_notification_type", columnList = "notification_type")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "user_id")
    private Long userId;  // 수신자 ID (User 참조)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "notification_type", length = 50)
    private NotificationType notificationType;  // 알림 타입
    
    @Column(nullable = false, length = 100)
    private String title;  // 알림 제목
    
    @Column(nullable = false, length = 500)
    private String content;  // 알림 내용
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;  // 관련 엔티티 타입 (예: "CHAT_ROOM", "PRODUCT", "POST", "COMMENT")
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;  // 관련 엔티티 ID (nullable)
    
    @Column(nullable = false, name = "is_read")
    private Boolean isRead = false;  // 읽음 여부 (기본값 false)
    
    @Column(name = "read_at")
    private LocalDateTime readAt;  // 읽은 시간 (nullable)
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Builder
    public Notification(
            Long userId,
            NotificationType notificationType,
            String title,
            String content,
            String relatedEntityType,
            Long relatedEntityId
    ) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.isRead = false;
        this.readAt = null;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 읽음 처리
     * 
     * 알림을 읽음 상태로 변경합니다.
     * isRead = true, readAt = 현재 시간으로 설정합니다.
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
