package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalReasonStatsDto;
import org.cmarket.cmarket.domain.auth.model.WithdrawalReasonType;

/**
 * 탈퇴 사유별 통계 응답 DTO
 */
@Getter
@Builder
public class WithdrawalReasonStatsResponse {
    private WithdrawalReasonType reason;
    private long count;

    public static WithdrawalReasonStatsResponse fromDto(WithdrawalReasonStatsDto dto) {
        return WithdrawalReasonStatsResponse.builder()
                .reason(dto.getReason())
                .count(dto.getCount())
                .build();
    }
}
