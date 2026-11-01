package org.cmarket.cmarket.domain.payment.app;

import lombok.Getter;

/**
 * 결제 승인이 안 됐다. code 는 토스 오류 코드, message 는 토스가 준 설명이다.
 *
 * retryable 이면 토스가 거절한 것이 아니라 **답을 못 받은 것**이다(연결 끊김, 시간 초과, 토스 서버 오류).
 * 이때는 토스가 실제로는 승인했을 수 있어, 주문을 실패로 닫지 않고 다시 확정하게 둔다(최종 검토 I2).
 */
@Getter
public class PaymentGatewayException extends RuntimeException {

    private final String code;
    private final boolean retryable;

    public PaymentGatewayException(String code, String message) {
        this(code, message, false);
    }

    public PaymentGatewayException(String code, String message, boolean retryable) {
        super(message);
        this.code = code;
        this.retryable = retryable;
    }
}
