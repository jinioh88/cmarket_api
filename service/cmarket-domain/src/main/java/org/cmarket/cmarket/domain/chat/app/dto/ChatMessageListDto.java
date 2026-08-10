package org.cmarket.cmarket.domain.chat.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 채팅 메시지 목록 DTO
 * 
 * 채팅 내역 조회 결과 (페이지네이션 포함)입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageListDto {
    
    private List<ChatMessageListItemDto> messages;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;

    /**
     * 내가 이 방의 상대를 차단했는가. (#877)
     *
     * 화면이 입력창을 잠그는 데 쓴다. **내가 차단했는지**만 본다 — 차단당한 쪽에는
     * 알리지 않기로 했으므로 반대 방향은 싣지 않는다.
     *
     * ⚠️ **원시형 boolean 이 아니라 Boolean 이다.** 원시형이면 Lombok 게터가
     * `isOpponentBlocked()` 가 되고, Jackson 이 `is` 를 떼어 **JSON 키가
     * `opponentBlocked` 로 나간다.** 화면은 `isOpponentBlocked` 를 찾으니 조용히 안 잠긴다.
     * 래퍼면 게터가 `getIsOpponentBlocked()` 라 키가 그대로다 — 옆의 `isBlocked` 도 같은 이유다.
     */
    private Boolean isOpponentBlocked;
}
