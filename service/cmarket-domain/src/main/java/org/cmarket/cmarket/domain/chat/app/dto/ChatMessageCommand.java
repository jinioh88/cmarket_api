package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.model.MessageType;

/**
 * 채팅 메시지 전송 커맨드 DTO
 * 
 * 웹 레이어에서 앱 레이어로 전달되는 메시지 전송 요청입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageCommand {
    
    private Long chatRoomId;
    private String content;
    private MessageType messageType;
    private String imageUrl;
}
