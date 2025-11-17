package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

/**
 * 거래 상태 변경 명령 DTO
 * 
 * 거래 상태 변경 시 필요한 정보를 담는 앱 DTO입니다.
 */
@Builder
public record TradeStatusUpdateCommand(
    TradeStatus tradeStatus
) {
}

