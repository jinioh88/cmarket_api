package org.cmarket.cmarket.web.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageDto;
import org.cmarket.cmarket.domain.chat.model.MessageType;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답 DTO
 * 
 * WebSocket으로 전송되는 메시지 형식입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    
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
     * 앱 DTO를 웹 응답 DTO로 변환
     */
    public static ChatMessageResponse from(ChatMessageDto dto) {
        return ChatMessageResponse.builder()
                .messageId(dto.getMessageId())
                .chatRoomId(dto.getChatRoomId())
                .senderId(dto.getSenderId())
                .senderNickname(dto.getSenderNickname())
                .messageType(dto.getMessageType())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .isBlocked(dto.getIsBlocked())
                .blockReason(dto.getBlockReason())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
