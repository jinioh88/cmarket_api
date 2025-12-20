package org.cmarket.cmarket.web.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.notification.app.event.NotificationCreatedEvent;
import org.cmarket.cmarket.domain.notification.app.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 알림 생성 이벤트 리스너
 * 
 * NotificationCreatedEvent 이벤트를 구독하여 알림을 생성합니다.
 * 서비스 로직과 알림 로직의 결합도를 낮추기 위해 Spring Event를 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    
    /**
     * 알림 생성 이벤트 처리
     * 
     * NotificationCreatedEvent 이벤트를 수신하여 알림을 생성합니다.
     * - RDB에 알림 저장
     * - 캐시 무효화 (해당 사용자의 알림 목록, 안 읽은 개수)
     * - SSE를 통한 실시간 전송
     * 
     * @param event 알림 생성 이벤트
     */
    @Async
    @EventListener
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        try {
            // 이벤트에서 NotificationCreateCommand 추출
            notificationService.createNotification(event.getCommand());
        } catch (Exception e) {
            log.error("알림 생성 실패: userId={}, error={}", event.getUserId(), e.getMessage(), e);
        }
    }
}
