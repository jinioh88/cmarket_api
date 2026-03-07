package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.WithdrawalReasonType;

/**
 * 탈퇴 사유별 통계 DTO
 */
@Getter
@Builder
public class WithdrawalReasonStatsDto {
    private WithdrawalReasonType reason;
    private long count;
}
