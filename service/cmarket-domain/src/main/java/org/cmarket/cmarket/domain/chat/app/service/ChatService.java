package org.cmarket.cmarket.domain.chat.app.service;

import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageListDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomCreateCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListDto;

/**
 * 채팅 서비스 인터페이스
 * 
 * 채팅 관련 비즈니스 로직을 정의합니다.
 */
public interface ChatService {
    
    /**
     * 채팅방 생성
     * 
     * 상품에 대한 채팅방을 생성합니다.
     * - 상품 존재 여부 확인
     * - 판매자 본인과의 채팅 방지
     * - 기존 채팅방 존재 시 기존 채팅방 반환
     * - 새 채팅방 생성 시 ChatRoom + ChatRoomUser 2개(구매자, 판매자) 생성
     * 
     * @param email 현재 로그인한 사용자 이메일 (구매자)
     * @param command 채팅방 생성 커맨드 (productId 포함)
     * @return 생성된 채팅방 정보
     */
    ChatRoomDto createChatRoom(String email, ChatRoomCreateCommand command);
    
    /**
     * 채팅방 목록 조회 (FR-024)
     * 
     * 사용자의 활성 채팅방 목록을 조회합니다.
     * - 활성 상태(isActive=true)인 채팅방만 조회
     * - 각 채팅방의 최근 메시지 포함
     * - Redis에서 안 읽은 메시지 개수 조회 (실시간)
     * - 최근 메시지 시간 기준 내림차순 정렬
     * 
     * @param email 현재 로그인한 사용자 이메일
     * @return 채팅방 목록
     */
    ChatRoomListDto getChatRoomList(String email);
    
    /**
     * 채팅방 참여자 여부 확인
     * 
     * 사용자가 해당 채팅방의 참여자인지 확인합니다.
     * WebSocket 구독/전송 권한 확인에 사용됩니다.
     * 
     * @param chatRoomId 채팅방 ID
     * @param email 사용자 이메일
     * @return 참여자이면 true, 아니면 false
     */
    boolean isParticipant(Long chatRoomId, String email);
    
    /**
     * 채팅 메시지 전송 (FR-026)
     * 
     * 채팅 메시지를 전송합니다.
     * - 채팅방 존재 및 참여 여부 확인
     * - 상대방이 채팅방을 나갔는지 확인
     * - 개인정보 필터링 (전화번호, 이메일, 계좌번호, 주민등록번호)
     * - 메시지 저장 (개인정보 포함 시 isBlocked = true)
     * - Redis에 상대방의 안 읽은 메시지 개수 증가
     * - ChatRoomUser의 lastMessage 정보 업데이트
     * 
     * @param email 발신자 이메일
     * @param command 메시지 전송 커맨드
     * @return 전송된 메시지 정보
     */
    ChatMessageDto sendMessage(String email, ChatMessageCommand command);
    
    /**
     * 사용자 ID로 참여자 여부 확인
     * 
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 참여자이면 true, 아니면 false
     */
    boolean isParticipantByUserId(Long chatRoomId, Long userId);
    
    /**
     * 채팅 내역 조회 (FR-028)
     * 
     * 채팅방의 메시지 내역을 조회합니다.
     * - 채팅방 존재 및 참여 여부 확인
     * - 첫 페이지 조회 시 Redis → RDB 읽음 상태 동기화
     * - 차단된 메시지는 발신자 본인에게만 표시
     * - 페이지네이션 지원 (최신순 → 오래된순 정렬)
     * 
     * @param email 현재 사용자 이메일
     * @param chatRoomId 채팅방 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 메시지 목록
     */
    ChatMessageListDto getChatMessages(String email, Long chatRoomId, int page, int size);
    
    /**
     * 채팅방 나가기 (FR-027)
     * 
     * 채팅방에서 나갑니다 (소프트 삭제).
     * - 채팅방 존재 및 참여 여부 확인
     * - ChatRoomUser의 isActive = false, leftAt 설정
     * - 시스템 메시지 저장 ("ㅇㅇ님이 채팅방을 나가셨습니다.")
     * - Redis에서 해당 사용자의 읽음 정보 삭제
     * 
     * @param email 현재 사용자 이메일
     * @param chatRoomId 채팅방 ID
     * @return 시스템 메시지 정보 (WebSocket 전송용)
     */
    ChatMessageDto leaveChatRoom(String email, Long chatRoomId);
}
