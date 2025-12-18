package org.cmarket.cmarket.domain.chat.repository;

import org.cmarket.cmarket.domain.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ChatMessage 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 
 * 주요 기능:
 * - 채팅방의 메시지 목록 조회 (페이지네이션)
 * - 채팅방의 최근 메시지 조회
 * - 읽음 상태 일괄 업데이트 (Redis → RDB Sync용)
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * 채팅방의 메시지 목록 조회 (페이지네이션, 최신순 정렬)
     * 
     * @param chatRoomId 채팅방 ID
     * @param pageable 페이지네이션 정보
     * @return 메시지 목록 (최신순 정렬)
     */
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);
    
    /**
     * 채팅방의 최근 메시지 조회
     * 
     * @param chatRoomId 채팅방 ID
     * @return 가장 최근 메시지 (없으면 Optional.empty())
     */
    Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
    
    /**
     * 안 읽은 메시지 일괄 읽음 처리 (Redis → RDB Sync용)
     * 
     * 특정 시점 이전의 안 읽은 메시지 중 본인이 받은 메시지를 일괄 읽음 처리합니다.
     * 채팅방 진입 시 Redis 상태를 RDB에 동기화할 때 사용합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 현재 사용자 ID (본인이 보낸 메시지 제외를 위해)
     * @param beforeTime 기준 시점 (이 시점 이전의 메시지 대상)
     * @return 업데이트된 메시지 개수
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true " +
           "WHERE m.chatRoomId = :chatRoomId " +
           "AND m.senderId != :userId " +
           "AND m.isRead = false " +
           "AND m.createdAt <= :beforeTime")
    int markMessagesAsRead(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId,
            @Param("beforeTime") LocalDateTime beforeTime
    );
    
    /**
     * 안 읽은 메시지 개수 조회 (RDB 기준)
     * 
     * Redis 장애 시 복구용 또는 검증용으로 사용합니다.
     * 본인이 받은 메시지(senderId != userId) 중 isRead = false인 메시지 개수를 조회합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 현재 사용자 ID
     * @return 안 읽은 메시지 개수
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "WHERE m.chatRoomId = :chatRoomId " +
           "AND m.senderId != :userId " +
           "AND m.isRead = false")
    int countUnreadMessages(
            @Param("chatRoomId") Long chatRoomId,
            @Param("userId") Long userId
    );
}
