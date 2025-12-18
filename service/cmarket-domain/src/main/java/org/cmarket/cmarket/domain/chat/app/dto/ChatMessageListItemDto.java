package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.model.ChatMessage;
import org.cmarket.cmarket.domain.chat.model.MessageType;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 목록 아이템 DTO
 * 
 * 채팅 내역 조회 시 개별 메시지 정보입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageListItemDto {
    
    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private MessageType messageType;
    private String content;
    private String imageUrl;
    private Boolean isBlocked;
    private String blockReason;
    private LocalDateTime createdAt;
    private Boolean isMine;  // 내가 보낸 메시지 여부
    
    /**
     * 엔티티를 DTO로 변환
     * 
     * @param message 메시지 엔티티
     * @param currentUserId 현재 사용자 ID (isMine 판단용)
     * @return DTO
     */
    public static ChatMessageListItemDto fromEntity(ChatMessage message, Long currentUserId) {
        return ChatMessageListItemDto.builder()
                .messageId(message.getId())
                .senderId(message.getSenderId())
                .senderNickname(message.getSenderNickname())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .isBlocked(message.getIsBlocked())
                .blockReason(message.getBlockReason())
                .createdAt(message.getCreatedAt())
                .isMine(message.getSenderId().equals(currentUserId))
                .build();
    }
}
