package org.cmarket.cmarket.domain.payment.app.dto;

/**
 * 결제 성공 주소로 돌아온 값. amount 는 믿지 않고 저장한 금액과 대조한다.
 */
public record PaymentConfirmCommand(String paymentKey, String orderId, long amount) {
}
