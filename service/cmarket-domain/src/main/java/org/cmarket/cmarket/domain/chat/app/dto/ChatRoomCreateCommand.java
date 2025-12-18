package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 생성 커맨드 DTO
 * 
 * 채팅방 생성에 필요한 정보를 담는 앱 계층 DTO입니다.
 */
@Getter
@Builder
public class ChatRoomCreateCommand {
    
    private Long productId;
}
