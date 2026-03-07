package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 대시보드 요약 통계 DTO
 */
@Getter
@Builder
public class StatisticsSummaryDto {
    private long totalUserCount;
    private long activeUserCount;
    private long withdrawnUserCount;
    private long totalProductCount;
    private long activeProductCount;
}
