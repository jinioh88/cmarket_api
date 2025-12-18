package org.cmarket.cmarket.web.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.model.MessageType;

/**
 * 채팅 메시지 전송 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    
    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long chatRoomId;
    
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다.")
    private String content;
    
    @NotNull(message = "메시지 타입은 필수입니다.")
    private MessageType messageType;
    
    /**
     * 이미지 URL (messageType = IMAGE일 때 사용)
     */
    private String imageUrl;
}
