package org.cmarket.cmarket.web.chat.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomDto;

import java.time.LocalDateTime;

/**
 * 채팅방 응답 DTO
 * 
 * 채팅방 생성 또는 조회 시 반환되는 웹 계층 DTO입니다.
 */
@Getter
@Builder
public class ChatRoomResponse {
    
    private Long chatRoomId;
    private Long productId;
    private String productTitle;
    private Long productPrice;
    private String productImageUrl;
    private LocalDateTime createdAt;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto ChatRoomDto
     * @return ChatRoomResponse
     */
    public static ChatRoomResponse fromDto(ChatRoomDto dto) {
        return ChatRoomResponse.builder()
                .chatRoomId(dto.getChatRoomId())
                .productId(dto.getProductId())
                .productTitle(dto.getProductTitle())
                .productPrice(dto.getProductPrice())
                .productImageUrl(dto.getProductImageUrl())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
