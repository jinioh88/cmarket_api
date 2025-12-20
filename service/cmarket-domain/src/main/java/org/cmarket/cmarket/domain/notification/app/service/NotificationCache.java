package org.cmarket.cmarket.domain.notification.app.service;

import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Pageable;

/**
 * 알림 캐시 서비스 인터페이스
 * 
 * 알림 데이터 캐싱을 담당하는 서비스입니다.
 * 구현체는 웹 계층에 위치합니다 (Caffeine Cache 등).
 */
public interface NotificationCache {
    
    /**
     * 알림 목록 조회 (캐시 우선)
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 캐시된 알림 목록 (없으면 null)
     */
    PageResult<NotificationDto> getNotificationList(Long userId, Pageable pageable);
    
    /**
     * 알림 목록 캐시 저장
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지네이션 정보
     * @param notificationList 알림 목록
     */
    void putNotificationList(Long userId, Pageable pageable, PageResult<NotificationDto> notificationList);
    
    /**
     * 안 읽은 알림 개수 조회 (캐시 우선)
     * 
     * @param userId 사용자 ID
     * @return 캐시된 안 읽은 알림 개수 (없으면 null)
     */
    Long getUnreadCount(Long userId);
    
    /**
     * 안 읽은 알림 개수 캐시 저장
     * 
     * @param userId 사용자 ID
     * @param unreadCount 안 읽은 알림 개수
     */
    void putUnreadCount(Long userId, Long unreadCount);
    
    /**
     * 사용자의 모든 알림 캐시 무효화
     * 
     * @param userId 사용자 ID
     */
    void evictAll(Long userId);
}
