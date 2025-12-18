package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 아이템 DTO
 * 
 * 채팅방 목록에서 각 채팅방의 정보를 담는 앱 계층 DTO입니다.
 */
@Getter
@Builder
public class ChatRoomListItemDto {
    
    // 채팅방 정보
    private Long chatRoomId;
    
    // 상품 정보 (스냅샷)
    private Long productId;
    private String productTitle;
    private Long productPrice;
    private String productImageUrl;
    
    // 상대방 정보
    private Long opponentId;
    private String opponentNickname;
    private String opponentProfileImageUrl;
    
    // 최근 메시지 정보
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    
    // 읽음 상태 (Redis에서 조회)
    private boolean hasUnread;
    private int unreadCount;
}
