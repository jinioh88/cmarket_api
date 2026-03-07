package org.cmarket.cmarket.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.admin.app.dto.MonthlyTrendDto;
import org.cmarket.cmarket.domain.admin.app.dto.StatisticsSummaryDto;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalReasonStatsDto;
import org.cmarket.cmarket.domain.admin.app.service.AdminStatisticsService;
import org.cmarket.cmarket.web.admin.dto.MonthlyTrendResponse;
import org.cmarket.cmarket.web.admin.dto.StatisticsSummaryResponse;
import org.cmarket.cmarket.web.admin.dto.WithdrawalReasonStatsResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 어드민 통계 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    /**
     * 월별 가입/탈퇴 추세
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/trends")
    public ResponseEntity<SuccessResponse<List<MonthlyTrendResponse>>> getMonthlyTrends(
            @RequestParam(required = false) String startMonth,
            @RequestParam(required = false) String endMonth
    ) {
        List<MonthlyTrendDto> dtoList = adminStatisticsService.getMonthlyTrends(startMonth, endMonth);
        List<MonthlyTrendResponse> response = dtoList.stream()
                .map(MonthlyTrendResponse::fromDto)
                .toList();
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    /**
     * 탈퇴 사유별 통계
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/withdrawal-reasons")
    public ResponseEntity<SuccessResponse<List<WithdrawalReasonStatsResponse>>> getWithdrawalReasonStats() {
        List<WithdrawalReasonStatsDto> dtoList = adminStatisticsService.getWithdrawalReasonStats();
        List<WithdrawalReasonStatsResponse> response = dtoList.stream()
                .map(WithdrawalReasonStatsResponse::fromDto)
                .toList();
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    /**
     * 대시보드 요약 통계
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<SuccessResponse<StatisticsSummaryResponse>> getSummary() {
        StatisticsSummaryDto dto = adminStatisticsService.getSummary();
        StatisticsSummaryResponse response = StatisticsSummaryResponse.fromDto(dto);
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}
