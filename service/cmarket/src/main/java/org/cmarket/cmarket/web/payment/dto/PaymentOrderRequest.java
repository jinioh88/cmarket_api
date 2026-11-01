package org.cmarket.cmarket.web.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentOrderRequest(@NotNull Long productId) {
}
