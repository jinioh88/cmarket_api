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

    /**
     * 내가 이 방의 상대를 차단했는가. (#877)
     *
     * 화면이 입력창을 잠그는 데 쓴다. **내가 차단했는지**만 본다 — 차단당한 쪽에는
     * 알리지 않기로 했으므로 반대 방향은 싣지 않는다.
     *
     * ⚠️ **원시형 boolean 이 아니라 Boolean 이다.** 원시형이면 Lombok 게터가
     * `isOpponentBlocked()` 가 되고, Jackson 이 `is` 를 떼어 **JSON 키가
     * `opponentBlocked` 로 나간다.** 화면은 `isOpponentBlocked` 를 찾으니 조용히 안 잠긴다.
     * 래퍼면 게터가 `getIsOpponentBlocked()` 라 키가 그대로다 — 옆의 `isBlocked` 도 같은 이유다.
     */
    private Boolean isOpponentBlocked;
}
