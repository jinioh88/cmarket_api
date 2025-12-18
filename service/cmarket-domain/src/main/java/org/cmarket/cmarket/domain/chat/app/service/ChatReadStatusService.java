package org.cmarket.cmarket.domain.chat.app.service;

import java.time.LocalDateTime;

/**
 * 채팅 읽음 상태 관리 서비스 인터페이스
 * 
 * Redis를 활용한 실시간 읽음 상태 관리 기능을 정의합니다.
 * 
 * 핵심 전략:
 * - 실시간 안 읽은 개수는 Redis에서 관리 (성능 최적화)
 * - 유저가 채팅방 진입 시 Redis 상태를 RDB에 일괄 동기화
 * 
 * Redis Key 구조:
 * - 안 읽은 개수: chat:unread:{chatRoomId}:{userId}
 * - 마지막 읽은 시간: chat:lastread:{chatRoomId}:{userId}
 * 
 * TTL: 30일 (메모리 관리)
 */
public interface ChatReadStatusService {
    
    /**
     * 안 읽은 메시지 개수 증가
     * 
     * 메시지 전송 시 수신자의 안 읽은 메시지 개수를 증가시킵니다.
     * 수신자가 해당 채팅방에 접속 중이면 증가하지 않습니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param recipientId 수신자 ID
     */
    void incrementUnreadCount(Long chatRoomId, Long recipientId);
    
    /**
     * 안 읽은 메시지 개수 조회
     * 
     * Redis에서 실시간 안 읽은 메시지 개수를 조회합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 안 읽은 메시지 개수 (없으면 0)
     */
    int getUnreadCount(Long chatRoomId, Long userId);
    
    /**
     * 안 읽은 메시지 개수 초기화
     * 
     * 채팅방 진입 시 안 읽은 메시지 개수를 0으로 초기화합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void resetUnreadCount(Long chatRoomId, Long userId);
    
    /**
     * 마지막 읽은 시간 조회
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 마지막 읽은 시간 (없으면 null)
     */
    LocalDateTime getLastReadTime(Long chatRoomId, Long userId);
    
    /**
     * 마지막 읽은 시간 업데이트
     * 
     * 채팅방 진입 시 또는 메시지 조회 시 마지막 읽은 시간을 업데이트합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void updateLastReadTime(Long chatRoomId, Long userId);
    
    /**
     * Redis → RDB 동기화
     * 
     * 채팅방 진입 시 Redis 상태를 RDB에 일괄 반영합니다.
     * - Redis의 unreadCount를 0으로 리셋
     * - RDB의 해당 채팅방 메시지 중 본인이 받은 메시지를 isRead = true로 업데이트
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void syncReadStatusToRdb(Long chatRoomId, Long userId);
    
    /**
     * 읽음 상태 정보 삭제
     * 
     * 채팅방 나가기 시 해당 사용자의 읽음 상태 정보를 삭제합니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    void deleteReadStatus(Long chatRoomId, Long userId);
}
