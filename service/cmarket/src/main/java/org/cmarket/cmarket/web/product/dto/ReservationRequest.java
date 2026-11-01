package org.cmarket.cmarket.web.product.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 예약 걸기 요청. 예약 상대 목록에서 고른 사람 번호 (#1126)
 */
public record ReservationRequest(@NotNull Long buyerId) {
}
