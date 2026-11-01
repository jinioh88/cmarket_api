package org.cmarket.cmarket.web.payment.dto;

import org.cmarket.cmarket.domain.payment.app.dto.PaymentResultDto;

import java.time.LocalDateTime;

public record PaymentResultResponse(String orderId, Long amount, Long productId, String productTitle,
                                    LocalDateTime approvedAt) {

    public static PaymentResultResponse fromDto(PaymentResultDto dto) {
        return new PaymentResultResponse(dto.orderId(), dto.amount(), dto.productId(), dto.productTitle(),
                dto.approvedAt());
    }
}
