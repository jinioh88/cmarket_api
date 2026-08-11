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
import org.cmarket.cmarket.domain.notification.model.NotificationType;
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
        // 0. 채팅은 한 방에 알림 하나로 묶는다 (#873)
        //
        // 자리를 비운 사이 메시지가 여러 개 오면 알림이 그만큼 쌓인다. 사용자에게는
        // 「그 사람과의 대화 하나」인데 목록이 한 사람으로 도배되고, 안 읽은 알림 수도
        // 그만큼 부풀어 오른다. 그래서 안 읽은 옛 알림을 지우고 새로 하나 만든다.
        // 몇 개가 밀렸는지는 문구에 적는다(ChatServiceImpl 에서 만든다).
        //
        // ⚠️ **갱신이 아니라 지우고 새로 만드는 이유**: createdAt 이 updatable = false 라
        //    갱신해도 목록에서 맨 위로 안 올라온다. 그 제약을 풀면 다른 알림도 생성 시각이
        //    바뀔 수 있는 문이 열린다.
        //
        // ⚠️ **읽은 알림은 안 건드린다.** 사용자가 이미 본 기록을 지우면 안 된다.
        if (command.getNotificationType() == NotificationType.CHAT_NEW_MESSAGE
                && command.getRelatedEntityId() != null) {
            notificationRepository.deleteUnreadByUserAndTypeAndEntity(
                    command.getUserId(),
                    NotificationType.CHAT_NEW_MESSAGE,
                    command.getRelatedEntityId()
            );
        }

        // 1. 알림 엔티티 생성 및 저장
        Notification notification = Notification.builder()
                .userId(command.getUserId())
                .notificationType(command.getNotificationType())
                .title(command.getTitle())
                .content(command.getContent())
                .relatedEntityType(command.getRelatedEntityType())
                .relatedEntityId(command.getRelatedEntityId())
                .groupCount(command.getGroupCount())
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

        // ⚠️ **이미 읽은 알림은 성공이다.** 전에는 여기서 「없음」(404)을 던졌다.
        //
        //    「읽음으로 만들어 달라」는 요청인데 이미 읽음이면 목적은 이미 이뤄졌다. 그런데도
        //    404 를 주니, 알림을 두 번 누르기만 해도 화면에 오류가 났다(#881).
        //    바로 위에서 알림이 있는 것과 내 것인 것을 이미 확인했으므로, 여기서 0이 나오는 경우는
        //    「이미 읽음」뿐이다.
        //
        //    없는 알림·남의 알림은 위 두 검사에서 걸러져 그대로 404 · 403 이다.
        if (updatedCount > 0) {
            // 5. 캐시 무효화 (데이터 정합성 보장)
            notificationCache.evictAll(userId);
        }
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
