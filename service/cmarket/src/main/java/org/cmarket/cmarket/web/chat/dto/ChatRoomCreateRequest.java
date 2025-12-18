package org.cmarket.cmarket.web.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomCreateCommand;

/**
 * 채팅방 생성 요청 DTO
 * 
 * 채팅방 생성 시 필요한 정보를 받습니다.
 * 구매자가 상품 상세 페이지에서 "채팅하기" 버튼을 누를 때 사용됩니다.
 */
@Getter
@NoArgsConstructor
public class ChatRoomCreateRequest {
    
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
    
    /**
     * 웹 DTO를 앱 DTO로 변환
     * 
     * @return ChatRoomCreateCommand
     */
    public ChatRoomCreateCommand toCommand() {
        return ChatRoomCreateCommand.builder()
                .productId(this.productId)
                .build();
    }
}
