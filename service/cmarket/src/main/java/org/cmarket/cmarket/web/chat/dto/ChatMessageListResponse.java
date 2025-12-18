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
                .build();
    }
}
