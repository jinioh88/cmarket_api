package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.StatisticsSummaryDto;

/**
 * 대시보드 요약 통계 응답 DTO
 */
@Getter
@Builder
public class StatisticsSummaryResponse {
    private long totalUserCount;
    private long activeUserCount;
    private long withdrawnUserCount;
    private long totalProductCount;
    private long activeProductCount;

    public static StatisticsSummaryResponse fromDto(StatisticsSummaryDto dto) {
        return StatisticsSummaryResponse.builder()
                .totalUserCount(dto.getTotalUserCount())
                .activeUserCount(dto.getActiveUserCount())
                .withdrawnUserCount(dto.getWithdrawnUserCount())
                .totalProductCount(dto.getTotalProductCount())
                .activeProductCount(dto.getActiveProductCount())
                .build();
    }
}
