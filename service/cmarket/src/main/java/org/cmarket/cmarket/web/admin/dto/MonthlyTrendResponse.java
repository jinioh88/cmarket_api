package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.MonthlyTrendDto;

/**
 * 월별 가입/탈퇴 추세 응답 DTO
 */
@Getter
@Builder
public class MonthlyTrendResponse {
    private String yearMonth;
    private long signupCount;
    private long withdrawalCount;

    public static MonthlyTrendResponse fromDto(MonthlyTrendDto dto) {
        return MonthlyTrendResponse.builder()
                .yearMonth(dto.getYearMonth())
                .signupCount(dto.getSignupCount())
                .withdrawalCount(dto.getWithdrawalCount())
                .build();
    }
}
