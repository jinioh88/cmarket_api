package org.cmarket.cmarket.domain.payment.app;

import lombok.Getter;

/**
 * 토스가 결제 승인을 거절했다. code 는 토스 오류 코드, message 는 토스가 준 설명이다.
 */
@Getter
public class PaymentGatewayException extends RuntimeException {

    private final String code;

    public PaymentGatewayException(String code, String message) {
        super(message);
        this.code = code;
    }
}
