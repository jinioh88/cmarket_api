package org.cmarket.cmarket.domain.notification.app.service;

import org.cmarket.cmarket.domain.notification.app.dto.NotificationCreateCommand;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Pageable;

/**
 * 알림 서비스 인터페이스
 * 
 * 알림 관련 비즈니스 로직을 정의합니다.
 * 추상화를 통해 향후 Redis 기반 구현으로 교체 가능하도록 설계합니다.
 */
public interface NotificationService {
    
    /**
     * 알림 생성
     * 
     * RDB에 알림을 저장하고 실시간으로 전송합니다.
     * 
     * @param command 알림 생성 명령
     */
    void createNotification(NotificationCreateCommand command);
    
    /**
     * 알림 목록 조회
     * 
     * 사용자의 알림 목록을 페이지네이션으로 조회합니다.
     * 
     * @param email 현재 로그인한 사용자 이메일
     * @param pageable 페이지네이션 정보
     * @return 알림 목록 (페이지네이션)
     */
    PageResult<NotificationDto> getNotificationList(String email, Pageable pageable);
    
    /**
     * 안 읽은 알림 개수 조회
     * 
     * @param email 현재 로그인한 사용자 이메일
     * @return 안 읽은 알림 개수
     */
    Long getUnreadCount(String email);
    
    /**
     * 알림 읽음 처리
     * 
     * 특정 알림을 읽음 상태로 변경합니다.
     * 
     * @param email 현재 로그인한 사용자 이메일
     * @param notificationId 알림 ID
     */
    void markAsRead(String email, Long notificationId);
    
    /**
     * 모든 알림 읽음 처리
     * 
     * 사용자의 모든 안 읽은 알림을 읽음 상태로 변경합니다.
     * 
     * @param email 현재 로그인한 사용자 이메일
     */
    void markAllAsRead(String email);
}
