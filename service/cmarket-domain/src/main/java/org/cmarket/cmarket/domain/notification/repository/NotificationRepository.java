package org.cmarket.cmarket.domain.notification.repository;

import org.cmarket.cmarket.domain.notification.model.Notification;
import org.cmarket.cmarket.domain.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Notification 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 
 * 주요 기능:
 * - 사용자별 알림 목록 조회 (페이지네이션)
 * - 사용자별 안 읽은 알림 개수 조회
 * - 사용자별 안 읽은 알림 목록 조회
 * - 알림 읽음 처리
 * - 사용자별 모든 알림 읽음 처리
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 사용자별 알림 목록 조회 (페이지네이션, 최신순 정렬)
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 알림 목록 (최신순 정렬)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 사용자별 안 읽은 알림 개수 조회
     * 
     * @param userId 사용자 ID
     * @return 안 읽은 알림 개수
     */
    long countByUserIdAndIsReadFalse(Long userId);
    
    /**
     * 사용자별 안 읽은 알림 목록 조회 (최신순 정렬)
     * 
     * @param userId 사용자 ID
     * @return 안 읽은 알림 목록 (최신순 정렬)
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    /**
     * 알림 읽음 처리
     * 
     * 특정 알림을 읽음 상태로 변경합니다.
     * 권한 확인을 위해 userId도 함께 체크합니다.
     * 
     * @param notificationId 알림 ID
     * @param userId 사용자 ID (권한 확인용)
     * @return 업데이트된 알림 개수 (0 또는 1)
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.id = :notificationId AND n.userId = :userId AND n.isRead = false")
    int markAsRead(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt
    );
    
    /**
     * 사용자별 모든 알림 읽음 처리
     * 
     * 특정 사용자의 모든 안 읽은 알림을 읽음 상태로 변경합니다.
     * 
     * @param userId 사용자 ID
     * @return 업데이트된 알림 개수
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt
    );

    /**
     * 보존 기간이 지난 "읽은" 알림 하드 삭제 (자동 삭제 대상 타입만)
     *
     * @param threshold readAt 이 이 값 이전이면 삭제 대상
     * @param autoDeletableTypes 자동 삭제 대상 타입 목록
     * @return 삭제된 알림 개수
     */
    @Modifying
    @Query("DELETE FROM Notification n " +
           "WHERE n.isRead = true " +
           "AND n.readAt < :threshold " +
           "AND n.notificationType IN :autoDeletableTypes")
    int deleteReadOlderThan(
            @Param("threshold") LocalDateTime threshold,
            @Param("autoDeletableTypes") Collection<NotificationType> autoDeletableTypes
    );

    /**
     * 보존 기간이 지난 "안 읽은" 알림 하드 삭제 (자동 삭제 대상 타입만)
     *
     * @param threshold createdAt 이 이 값 이전이면 삭제 대상
     * @param autoDeletableTypes 자동 삭제 대상 타입 목록
     * @return 삭제된 알림 개수
     */
    @Modifying
    @Query("DELETE FROM Notification n " +
           "WHERE n.isRead = false " +
           "AND n.createdAt < :threshold " +
           "AND n.notificationType IN :autoDeletableTypes")
    int deleteUnreadOlderThan(
            @Param("threshold") LocalDateTime threshold,
            @Param("autoDeletableTypes") Collection<NotificationType> autoDeletableTypes
    );
}
