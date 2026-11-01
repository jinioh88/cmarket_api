package org.cmarket.cmarket.web.chat.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListItemDto;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 아이템 응답 DTO
 * 
 * 채팅방 목록에서 각 채팅방의 정보를 담는 웹 계층 DTO입니다.
 */
@Getter
@Builder
public class ChatRoomListItemResponse {
    
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
    
    // 읽음 상태
    private boolean hasUnread;
    private int unreadCount;

    /** 내가 이 방의 상대를 차단했는가. 화면이 입력창을 잠그는 데 쓴다. (#877) */
    private Boolean isOpponentBlocked;

    /** 이 방이 지금 열려 있는지 (#1126). OPEN · PAUSED_RESERVED · CLOSED_SOLD · CLOSED_DONE */
    private String roomState;

    /** 지금 보는 사람이 이 방에서 결제할 수 있는가 (#1126) */
    private Boolean canPay;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto ChatRoomListItemDto
     * @return ChatRoomListItemResponse
     */
    public static ChatRoomListItemResponse fromDto(ChatRoomListItemDto dto) {
        return ChatRoomListItemResponse.builder()
                .chatRoomId(dto.getChatRoomId())
                .productId(dto.getProductId())
                .productTitle(dto.getProductTitle())
                .productPrice(dto.getProductPrice())
                .productImageUrl(dto.getProductImageUrl())
                .opponentId(dto.getOpponentId())
                .opponentNickname(dto.getOpponentNickname())
                .opponentProfileImageUrl(dto.getOpponentProfileImageUrl())
                .lastMessage(dto.getLastMessage())
                .lastMessageTime(dto.getLastMessageTime())
                .hasUnread(dto.isHasUnread())
                .unreadCount(dto.getUnreadCount())
                .isOpponentBlocked(dto.getIsOpponentBlocked())
                .roomState(dto.getRoomState())
                .canPay(dto.getCanPay())
                .build();
    }
}
