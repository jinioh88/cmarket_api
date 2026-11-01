package org.cmarket.cmarket.domain.product.app.dto;

import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;

import java.util.List;

/**
 * 거래 상태를 바꾼 결과. 예약이 풀리면 방마다 남긴 한 줄이 notices 에 담긴다.
 */
public record TradeStatusChangeResult(ProductDto product, List<ChatSystemNotice> notices) {
}
