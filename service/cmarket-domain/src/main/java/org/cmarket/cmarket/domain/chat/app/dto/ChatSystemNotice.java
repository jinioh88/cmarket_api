package org.cmarket.cmarket.domain.chat.app.dto;

/**
 * 방에 남긴 한 줄. 웹 계층이 이 목록을 받아 웹소켓으로 보낸다(나가기와 같은 방식).
 */
public record ChatSystemNotice(Long chatRoomId, String content) {
}
