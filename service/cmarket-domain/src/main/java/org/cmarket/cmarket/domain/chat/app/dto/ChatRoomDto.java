package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.chat.model.ChatRoom;

import java.time.LocalDateTime;

/**
 * 채팅방 정보 DTO
 * 
 * 채팅방 기본 정보를 담는 앱 계층 DTO입니다.
 */
@Getter
@Builder
public class ChatRoomDto {
    
    private Long chatRoomId;
    private Long productId;
    private String productTitle;
    private Long productPrice;
    private String productImageUrl;
    private String sellerNickname;
    private String sellerProfileImageUrl;
    private LocalDateTime createdAt;
    
    /**
     * ChatRoom 엔티티와 판매자 정보를 DTO로 변환
     * 
     * @param chatRoom 채팅방 엔티티
     * @param sellerNickname 판매자 닉네임
     * @param sellerProfileImageUrl 판매자 프로필 이미지 URL
     * @return ChatRoomDto
     */
    public static ChatRoomDto fromEntity(ChatRoom chatRoom, String sellerNickname, String sellerProfileImageUrl) {
        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getId())
                .productId(chatRoom.getProductId())
                .productTitle(chatRoom.getProductTitle())
                .productPrice(chatRoom.getProductPrice())
                .productImageUrl(chatRoom.getProductImageUrl())
                .sellerNickname(sellerNickname)
                .sellerProfileImageUrl(sellerProfileImageUrl)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
