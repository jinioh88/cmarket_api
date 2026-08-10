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
                .build();
    }
}
