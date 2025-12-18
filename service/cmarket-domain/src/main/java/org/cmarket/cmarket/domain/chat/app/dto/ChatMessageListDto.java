package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 채팅 메시지 목록 DTO
 * 
 * 채팅 내역 조회 결과 (페이지네이션 포함)입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageListDto {
    
    private List<ChatMessageListItemDto> messages;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
}
