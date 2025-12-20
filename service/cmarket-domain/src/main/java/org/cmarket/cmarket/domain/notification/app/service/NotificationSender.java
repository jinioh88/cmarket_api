package org.cmarket.cmarket.domain.notification.app.service;

import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;

/**
 * 알림 전송 서비스 인터페이스
 * 
 * 실시간 알림 전송을 담당하는 서비스입니다.
 * 구현체는 웹 계층에 위치합니다 (SSE, WebSocket 등).
 */
public interface NotificationSender {
    
    /**
     * 특정 사용자에게 알림 전송
     * 
     * @param userId 사용자 ID
     * @param notificationDto 알림 DTO
     */
    void sendNotification(Long userId, NotificationDto notificationDto);
}
