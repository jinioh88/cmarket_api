package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 채팅방 목록 DTO
 * 
 * 사용자의 채팅방 목록을 담는 앱 계층 DTO입니다.
 */
@Getter
@Builder
public class ChatRoomListDto {
    
    private List<ChatRoomListItemDto> chatRooms;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
}
