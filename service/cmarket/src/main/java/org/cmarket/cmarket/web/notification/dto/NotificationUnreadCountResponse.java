package org.cmarket.cmarket.web.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 안 읽은 알림 개수 응답 DTO
 * 
 * 안 읽은 알림 개수 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class NotificationUnreadCountResponse {
    private Long unreadCount;
    
    public NotificationUnreadCountResponse(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
