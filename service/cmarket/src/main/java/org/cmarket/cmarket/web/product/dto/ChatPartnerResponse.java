package org.cmarket.cmarket.web.product.dto;

import org.cmarket.cmarket.domain.product.app.dto.ChatPartnerDto;

import java.time.LocalDateTime;

/**
 * 예약 상대 후보 응답 (판매자 본인만 받는다, #1126)
 */
public record ChatPartnerResponse(Long userId, String nickname, String profileImageUrl, LocalDateTime lastMessageAt) {

    public static ChatPartnerResponse fromDto(ChatPartnerDto dto) {
        return new ChatPartnerResponse(dto.userId(), dto.nickname(), dto.profileImageUrl(), dto.lastMessageAt());
    }
}
