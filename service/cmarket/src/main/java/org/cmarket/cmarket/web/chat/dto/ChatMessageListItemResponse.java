package org.cmarket.cmarket.web.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageListItemDto;
import org.cmarket.cmarket.domain.chat.model.MessageType;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 목록 아이템 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageListItemResponse {
    
    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private MessageType messageType;
    private String content;
    private String imageUrl;
    private Boolean isBlocked;
    private String blockReason;
    private LocalDateTime createdAt;
    private Boolean isMine;
    
    public static ChatMessageListItemResponse from(ChatMessageListItemDto dto) {
        return ChatMessageListItemResponse.builder()
                .messageId(dto.getMessageId())
                .senderId(dto.getSenderId())
                .senderNickname(dto.getSenderNickname())
                .messageType(dto.getMessageType())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .isBlocked(dto.getIsBlocked())
                .blockReason(dto.getBlockReason())
                .createdAt(dto.getCreatedAt())
                .isMine(dto.getIsMine())
                .build();
    }
}
