package org.cmarket.cmarket.web.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cmarket.cmarket.domain.product.app.dto.TradeStatusUpdateCommand;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

/**
 * 거래 상태 변경 요청 DTO
 * 
 * 거래 상태 변경 시 필요한 정보를 받습니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class TradeStatusUpdateRequest {
    
    @NotNull(message = "거래 상태는 필수입니다.")
    private TradeStatus tradeStatus;
    
    /**
     * 웹 DTO를 앱 DTO로 변환
     * 
     * @return TradeStatusUpdateCommand
     */
    public TradeStatusUpdateCommand toCommand() {
        return TradeStatusUpdateCommand.builder()
                .tradeStatus(this.tradeStatus)
                .build();
    }
}

