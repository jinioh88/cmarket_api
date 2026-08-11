package org.cmarket.cmarket.web.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageListDto;

import java.util.List;

/**
 * 채팅 메시지 목록 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageListResponse {
    
    private List<ChatMessageListItemResponse> messages;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;

    /** 내가 이 방의 상대를 차단했는가. 화면이 입력창을 잠그는 데 쓴다. (#877) */
    private Boolean isOpponentBlocked;

    /**
     * 상대와 상품. 앱 채팅방 머리말에 쓴다. (#889)
     *
     * 앱은 방 번호만 들고 들어와 메시지만 조회하므로, 여기서 안 실어 주면
     * 누구와 무슨 상품 이야기를 하는지 화면에 못 그린다.
     */
    private Long opponentId;
    private String opponentNickname;
    private String opponentProfileImageUrl;

    private Long productId;
    private String productTitle;
    private Long productPrice;
    private String productImageUrl;
    
    public static ChatMessageListResponse from(ChatMessageListDto dto) {
        List<ChatMessageListItemResponse> messages = dto.getMessages().stream()
                .map(ChatMessageListItemResponse::from)
                .toList();
        
        return ChatMessageListResponse.builder()
                .messages(messages)
                .currentPage(dto.getCurrentPage())
                .totalPages(dto.getTotalPages())
                .totalElements(dto.getTotalElements())
                .hasNext(dto.isHasNext())
                .hasPrevious(dto.isHasPrevious())
                .isOpponentBlocked(dto.getIsOpponentBlocked())
                .opponentId(dto.getOpponentId())
                .opponentNickname(dto.getOpponentNickname())
                .opponentProfileImageUrl(dto.getOpponentProfileImageUrl())
                .productId(dto.getProductId())
                .productTitle(dto.getProductTitle())
                .productPrice(dto.getProductPrice())
                .productImageUrl(dto.getProductImageUrl())
                .build();
    }
}
