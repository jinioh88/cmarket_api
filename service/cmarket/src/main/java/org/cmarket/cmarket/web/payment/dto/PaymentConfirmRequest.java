package org.cmarket.cmarket.web.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentConfirmCommand;

/**
 * 결제 성공 주소로 돌아온 세 값. amount 는 서버가 저장한 금액과 대조한다.
 */
public record PaymentConfirmRequest(@NotBlank String paymentKey, @NotBlank String orderId, @NotNull Long amount) {

    public PaymentConfirmCommand toCommand() {
        return new PaymentConfirmCommand(paymentKey, orderId, amount);
    }
}
