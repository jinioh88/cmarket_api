package org.cmarket.cmarket.web.notification.service;

import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;
import org.cmarket.cmarket.domain.notification.app.service.NotificationSender;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 연결 관리 서비스
 * 
 * Server-Sent Events를 통한 실시간 알림 전송을 관리합니다.
 * NotificationSender 인터페이스의 SSE 구현체입니다.
 * 
 * Step 6에서 완전히 구현됩니다.
 */
public interface NotificationSseService extends NotificationSender {
    
    /**
     * SSE 연결 생성 및 관리
     * 
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    SseEmitter connect(Long userId);
    
    /**
     * 특정 사용자에게 알림 전송
     * 
     * @param userId 사용자 ID
     * @param notificationDto 알림 DTO
     */
    void sendNotification(Long userId, NotificationDto notificationDto);
    
    /**
     * SSE 연결 해제
     * 
     * @param userId 사용자 ID
     */
    void disconnect(Long userId);
    
    /**
     * 사용자 연결 여부 확인
     * 
     * @param userId 사용자 ID
     * @return 연결되어 있으면 true, 아니면 false
     */
    boolean isConnected(Long userId);
}
