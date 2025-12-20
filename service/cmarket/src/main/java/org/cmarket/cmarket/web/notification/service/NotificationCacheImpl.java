package org.cmarket.cmarket.web.notification.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;
import org.cmarket.cmarket.domain.notification.app.service.NotificationCache;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 알림 캐시 서비스 구현체
 * 
 * Caffeine Cache를 사용한 NotificationCache 인터페이스의 구현체입니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationCacheImpl implements NotificationCache {
    
    @Qualifier("notificationListCache")
    private final Cache<Long, Page<NotificationDto>> notificationListCache;
    
    @Qualifier("notificationUnreadCountCache")
    private final Cache<Long, Long> notificationUnreadCountCache;
    
    @Override
    public PageResult<NotificationDto> getNotificationList(Long userId, Pageable pageable) {
        // 0페이지만 캐싱
        if (pageable.getPageNumber() != 0) {
            return null;
        }
        
        Page<NotificationDto> cachedPage = notificationListCache.getIfPresent(userId);
        if (cachedPage == null) {
            return null;
        }
        
        // Page를 PageResult로 변환
        return PageResult.fromPage(cachedPage);
    }
    
    @Override
    public void putNotificationList(Long userId, Pageable pageable, PageResult<NotificationDto> notificationList) {
        // 0페이지만 캐싱
        if (pageable.getPageNumber() != 0) {
            return;
        }
        
        // PageResult를 Page로 변환하여 캐시에 저장
        // PageResult의 content를 Page로 변환
        Page<NotificationDto> page = new org.springframework.data.domain.PageImpl<>(
                notificationList.content(),
                pageable,
                notificationList.total()
        );
        
        notificationListCache.put(userId, page);
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return notificationUnreadCountCache.getIfPresent(userId);
    }
    
    @Override
    public void putUnreadCount(Long userId, Long unreadCount) {
        notificationUnreadCountCache.put(userId, unreadCount);
    }
    
    @Override
    public void evictAll(Long userId) {
        notificationListCache.invalidate(userId);
        notificationUnreadCountCache.invalidate(userId);
    }
}
