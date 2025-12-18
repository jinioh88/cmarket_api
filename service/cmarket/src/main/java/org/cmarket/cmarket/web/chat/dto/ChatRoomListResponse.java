package org.cmarket.cmarket.web.chat.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅방 목록 응답 DTO
 * 
 * 사용자의 채팅방 목록을 담는 웹 계층 DTO입니다.
 */
@Getter
@Builder
public class ChatRoomListResponse {
    
    private List<ChatRoomListItemResponse> chatRooms;
    private int totalCount;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto ChatRoomListDto
     * @return ChatRoomListResponse
     */
    public static ChatRoomListResponse fromDto(ChatRoomListDto dto) {
        List<ChatRoomListItemResponse> chatRooms = dto.getChatRooms().stream()
                .map(ChatRoomListItemResponse::fromDto)
                .collect(Collectors.toList());
        
        return ChatRoomListResponse.builder()
                .chatRooms(chatRooms)
                .totalCount(dto.getTotalCount())
                .build();
    }
}
