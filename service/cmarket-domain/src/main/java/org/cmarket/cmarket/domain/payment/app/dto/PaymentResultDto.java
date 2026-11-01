package org.cmarket.cmarket.domain.payment.app.dto;

import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 확정 결과. notices 는 예약자 방에 남긴 「결제가 완료됐어요」 (웹 계층이 웹소켓으로 보낸다).
 */
public record PaymentResultDto(String orderId, Long amount, Long productId, String productTitle,
                               LocalDateTime approvedAt, List<ChatSystemNotice> notices) {
}
