package org.cmarket.cmarket.domain.payment.app;

import java.time.LocalDateTime;

/**
 * 결제 승인 창구. 구현은 웹 계층의 TossPaymentGateway 다 (토스 결제 승인 API 를 부른다).
 * 시험에서는 가짜로 바꿔 끼운다.
 */
public interface PaymentGateway {

    /**
     * @return 승인 시각
     * @throws PaymentGatewayException 토스가 거절했을 때
     */
    LocalDateTime confirm(String paymentKey, String orderId, long amount);
}
