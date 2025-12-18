package org.cmarket.cmarket.domain.chat.app.service;

/**
 * 채팅 세션 관리 서비스 인터페이스
 * 
 * Redis를 활용한 사용자 세션 정보 관리 기능을 정의합니다.
 * 
 * 용도:
 * - 사용자 온라인 상태 추적
 * - 현재 접속 중인 채팅방 정보 관리
 * - 메시지 전송 시 수신자 온라인 여부 확인
 * 
 * Redis Key 구조:
 * - 세션: chat:session:{userId}
 * - 현재 채팅방: chat:current:{userId}
 */
public interface ChatSessionService {
    
    /**
     * 사용자 세션 등록
     * 
     * WebSocket 연결 시 사용자 세션 정보를 Redis에 저장합니다.
     * 
     * @param userId 사용자 ID
     * @param sessionId WebSocket 세션 ID
     */
    void addUserSession(Long userId, String sessionId);
    
    /**
     * 사용자 세션 제거
     * 
     * WebSocket 연결 해제 시 사용자 세션 정보를 Redis에서 제거합니다.
     * 현재 채팅방 정보도 함께 제거됩니다.
     * 
     * @param userId 사용자 ID
     * @param sessionId WebSocket 세션 ID
     */
    void removeUserSession(Long userId, String sessionId);
    
    /**
     * 사용자 온라인 여부 확인
     * 
     * @param userId 사용자 ID
     * @return 온라인이면 true, 오프라인이면 false
     */
    boolean isUserOnline(Long userId);
    
    /**
     * 현재 접속 중인 채팅방 조회
     * 
     * @param userId 사용자 ID
     * @return 현재 채팅방 ID (접속 중인 채팅방이 없으면 null)
     */
    Long getUserCurrentChatRoom(Long userId);
    
    /**
     * 현재 채팅방 설정
     * 
     * 사용자가 특정 채팅방에 진입할 때 호출합니다.
     * 이 정보는 메시지 수신 시 실시간 읽음 처리에 사용됩니다.
     * 
     * @param userId 사용자 ID
     * @param chatRoomId 채팅방 ID (null이면 채팅방에서 나감)
     */
    void setUserCurrentChatRoom(Long userId, Long chatRoomId);
    
    /**
     * 현재 채팅방 초기화
     * 
     * 사용자가 채팅방에서 나갈 때 호출합니다.
     * 
     * @param userId 사용자 ID
     */
    void clearUserCurrentChatRoom(Long userId);
}
