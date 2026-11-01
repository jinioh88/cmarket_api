package org.cmarket.cmarket.domain.payment.app.dto;

/**
 * 결제창을 열 때 쓰는 주문. 금액은 서버가 상품 가격으로 정했다.
 */
public record PaymentOrderDto(String orderId, Long amount, String orderName) {
}
