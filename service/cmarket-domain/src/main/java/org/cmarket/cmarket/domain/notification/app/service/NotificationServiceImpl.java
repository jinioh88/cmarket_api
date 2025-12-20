package org.cmarket.cmarket.domain.notification.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationCreateCommand;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;
import org.cmarket.cmarket.domain.notification.app.exception.NotificationAccessDeniedException;
import org.cmarket.cmarket.domain.notification.app.exception.NotificationNotFoundException;
import org.cmarket.cmarket.domain.notification.model.Notification;
import org.cmarket.cmarket.domain.notification.repository.NotificationRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 서비스 구현체
 * 
 * 알림 관련 비즈니스 로직을 구현합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final NotificationCache notificationCache;
    private final UserRepository userRepository;
    
    @Override
    @Async
    @Transactional
    public void createNotification(NotificationCreateCommand command) {
        // 1. 알림 엔티티 생성 및 저장
        Notification notification = Notification.builder()
                .userId(command.getUserId())
                .notificationType(command.getNotificationType())
                .title(command.getTitle())
                .content(command.getContent())
                .relatedEntityType(command.getRelatedEntityType())
                .relatedEntityId(command.getRelatedEntityId())
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // 2. NotificationDto로 변환
        NotificationDto notificationDto = NotificationDto.fromEntity(savedNotification);
        
        // 3. 캐시 무효화 (해당 사용자의 알림 목록, 안 읽은 개수)
        notificationCache.evictAll(command.getUserId());
        
        // 4. 실시간 전송 (SSE 등)
        notificationSender.sendNotification(command.getUserId(), notificationDto);
    }
    
    @Override
    public PageResult<NotificationDto> getNotificationList(String email, Pageable pageable) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException());
        Long userId = user.getId();
        
        // 2. 캐시에서 조회 시도
        PageResult<NotificationDto> cachedResult = notificationCache.getNotificationList(userId, pageable);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 3. RDB에서 조회
        Page<Notification> notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // 4. NotificationDto로 변환하여 PageResult 생성
        Page<NotificationDto> notificationDtoPage = notificationPage.map(NotificationDto::fromEntity);
        PageResult<NotificationDto> result = PageResult.fromPage(notificationDtoPage);
        
        // 5. 캐시에 저장 (0페이지만 캐싱)
        notificationCache.putNotificationList(userId, pageable, result);
        
        return result;
    }
    
    @Override
    public Long getUnreadCount(String email) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException());
        Long userId = user.getId();
        
        // 2. 캐시에서 조회 시도
        Long cachedCount = notificationCache.getUnreadCount(userId);
        if (cachedCount != null) {
            return cachedCount;
        }
        
        // 3. RDB에서 조회
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        
        // 4. 캐시에 저장
        notificationCache.putUnreadCount(userId, unreadCount);
        
        return unreadCount;
    }
    
    @Override
    @Transactional
    public void markAsRead(String email, Long notificationId) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException());
        Long userId = user.getId();
        
        // 2. 알림 존재 여부 및 소유자 확인
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException());
        
        // 3. 권한 확인 (본인의 알림인지 확인)
        if (!notification.getUserId().equals(userId)) {
            throw new NotificationAccessDeniedException();
        }
        
        // 4. RDB에서 알림 읽음 처리
        int updatedCount = notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
        
        if (updatedCount == 0) {
            // 이미 읽음 처리된 경우 (동시성 이슈 등)
            throw new NotificationNotFoundException("이미 읽음 처리된 알림입니다.");
        }
        
        // 5. 캐시 무효화 (데이터 정합성 보장)
        notificationCache.evictAll(userId);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(String email) {
        // 1. 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException());
        Long userId = user.getId();
        
        // 2. RDB에서 모든 알림 읽음 처리
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        
        // 3. 캐시 무효화 (데이터 정합성 보장)
        notificationCache.evictAll(userId);
    }
}
