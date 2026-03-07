package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 월별 가입/탈퇴 추세 DTO
 */
@Getter
@Builder
public class MonthlyTrendDto {
    private String yearMonth;  // "2025-01"
    private long signupCount;
    private long withdrawalCount;
}
