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
     * 상대가 나를 차단해서 상대에게 전달하면 안 되는 메시지인가. (#877)
     *
     * ⚠️ **서버 안에서만 쓰는 값이다.** 전달 통로를 고르려고 컨트롤러에 알리는 것이고,
     * `ChatMessageResponse` 로는 내보내지 않는다 — 내보내면 **차단당한 쪽이 차단 사실을
     * 알게 된다.** 알리지 않기로 한 이유는 보복을 부르기 때문이다.
     */
    private Boolean blockedByOpponent;
    
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
                .blockedByOpponent(false)
                .build();
    }

    /**
     * 상대가 나를 차단했는지까지 실어서 만든다. (#877)
     *
     * 전달 통로를 고르는 것은 컨트롤러라, 서비스가 여기에 표시해서 넘긴다.
     */
    public static ChatMessageDto fromEntity(ChatMessage message, boolean blockedByOpponent) {
        ChatMessageDto dto = fromEntity(message);
        return ChatMessageDto.builder()
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
                .blockedByOpponent(blockedByOpponent)
                .build();
    }
}
