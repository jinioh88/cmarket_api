package org.cmarket.cmarket.domain.notification.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.notification.model.NotificationType;
import org.cmarket.cmarket.domain.notification.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 자동 삭제 스케줄러
 *
 * 정책:
 * - 읽은 알림: readAt 기준 30일 이후 삭제
 * - 안 읽은 알림: createdAt 기준 90일 이후 삭제
 * - {@link NotificationType#isAutoDeletable()} 가 false 인 타입(거래/이력성)은 자동 삭제 대상에서 제외
 *
 * 실행 시점: 매일 새벽 4시 (서버 타임존 기준)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private static final int READ_RETENTION_DAYS = 30;
    private static final int UNREAD_RETENTION_DAYS = 90;

    private final NotificationRepository notificationRepository;

    /**
     * 알림 자동 삭제 작업
     *
     * 매일 새벽 4시에 실행됩니다.
     * 자동 삭제 대상 타입에 한해 보존 기간이 지난 알림을 하드 삭제합니다.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readThreshold = now.minusDays(READ_RETENTION_DAYS);
        LocalDateTime unreadThreshold = now.minusDays(UNREAD_RETENTION_DAYS);

        List<NotificationType> autoDeletableTypes = Arrays.stream(NotificationType.values())
                .filter(NotificationType::isAutoDeletable)
                .collect(Collectors.toList());

        if (autoDeletableTypes.isEmpty()) {
            log.info("Notification cleanup skipped: no auto-deletable types configured");
            return;
        }

        int deletedRead = notificationRepository.deleteReadOlderThan(readThreshold, autoDeletableTypes);
        int deletedUnread = notificationRepository.deleteUnreadOlderThan(unreadThreshold, autoDeletableTypes);

        log.info(
                "Notification cleanup completed: deletedRead={}, deletedUnread={}, readThreshold={}, unreadThreshold={}",
                deletedRead,
                deletedUnread,
                readThreshold,
                unreadThreshold
        );
    }
}
