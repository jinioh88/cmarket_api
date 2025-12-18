package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.model.ChatMessage;
import org.cmarket.cmarket.domain.chat.model.MessageType;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 DTO
 * 
 * 앱 레이어에서 사용하는 메시지 정보입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    
    private Long messageId;
    private Long chatRoomId;
    private Long senderId;
    private String senderNickname;
    private MessageType messageType;
    private String content;
    private String imageUrl;
    private Boolean isBlocked;
    private String blockReason;
    private LocalDateTime createdAt;
    
    /**
     * 엔티티를 DTO로 변환
     */
    public static ChatMessageDto fromEntity(ChatMessage message) {
        return ChatMessageDto.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .senderNickname(message.getSenderNickname())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .isBlocked(message.getIsBlocked())
                .blockReason(message.getBlockReason())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
