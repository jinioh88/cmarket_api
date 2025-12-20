package org.cmarket.cmarket.web.notification.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import java.util.concurrent.TimeUnit;

/**
 * 알림 캐시 설정
 * 
 * Caffeine Cache를 사용한 알림 데이터 캐싱 설정입니다.
 * 
 * 캐시 구조:
 * - 알림 목록 캐시: Cache<Long, Page<NotificationDto>> (Key: userId)
 * - 안 읽은 개수 캐시: Cache<Long, Long> (Key: userId)
 * 
 * 캐시 전략:
 * - Cache-Aside 패턴 사용
 * - 조회 시: 캐시 먼저 확인 → 미스 시 RDB 조회 → 캐시 저장
 * - 쓰기 시: RDB 저장 → 캐시 무효화 (evict)
 */
@Configuration
public class NotificationCacheConfig {
    
    /**
     * 알림 목록 캐시 빈 생성
     * 
     * 사용자별 알림 목록을 캐싱합니다.
     * 
     * 설정:
     * - 최대 크기: 1000개
     * - TTL: 5분
     * 
     * @return 알림 목록 캐시
     */
    @Bean(name = "notificationListCache")
    public Cache<Long, Page<NotificationDto>> notificationListCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }
    
    /**
     * 안 읽은 알림 개수 캐시 빈 생성
     * 
     * 사용자별 안 읽은 알림 개수를 캐싱합니다.
     * 
     * 설정:
     * - 최대 크기: 5000개
     * - TTL: 1분
     * 
     * @return 안 읽은 알림 개수 캐시
     */
    @Bean(name = "notificationUnreadCountCache")
    public Cache<Long, Long> notificationUnreadCountCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }
}
