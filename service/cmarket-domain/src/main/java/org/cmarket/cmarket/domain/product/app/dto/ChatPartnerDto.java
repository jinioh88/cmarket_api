package org.cmarket.cmarket.domain.product.app.dto;

import java.time.LocalDateTime;

/**
 * 예약 상대 후보 (이 상품으로 판매자와 채팅한 사람). 닉네임·사진은 채팅방에 저장된 값이다.
 */
public record ChatPartnerDto(Long userId, String nickname, String profileImageUrl, LocalDateTime lastMessageAt) {
}
