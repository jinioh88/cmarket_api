package org.cmarket.cmarket.domain.admin.app.service;

import org.cmarket.cmarket.domain.admin.app.dto.MonthlyTrendDto;
import org.cmarket.cmarket.domain.admin.app.dto.StatisticsSummaryDto;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalReasonStatsDto;
import org.cmarket.cmarket.domain.auth.model.WithdrawalReasonType;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 어드민 통계 서비스
 */
@Service
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public AdminStatisticsService(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<MonthlyTrendDto> getMonthlyTrends(String startMonth, String endMonth) {
        YearMonth start = startMonth != null && !startMonth.isBlank()
                ? YearMonth.parse(startMonth)
                : YearMonth.now().minusMonths(11);
        YearMonth end = endMonth != null && !endMonth.isBlank()
                ? YearMonth.parse(endMonth)
                : YearMonth.now();

        if (start.isAfter(end)) {
            YearMonth temp = start;
            start = end;
            end = temp;
        }

        List<MonthlyTrendDto> result = new ArrayList<>();
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999);

            long signupCount = userRepository.countByCreatedAtBetween(monthStart, monthEnd);
            long withdrawalCount = userRepository.countByDeletedAtBetween(monthStart, monthEnd);

            result.add(MonthlyTrendDto.builder()
                    .yearMonth(ym.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .signupCount(signupCount)
                    .withdrawalCount(withdrawalCount)
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<WithdrawalReasonStatsDto> getWithdrawalReasonStats() {
        List<Object[]> rows = userRepository.countByWithdrawalReason();
        List<WithdrawalReasonStatsDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            WithdrawalReasonType reason = (WithdrawalReasonType) row[0];
            Long count = (Long) row[1];
            result.add(WithdrawalReasonStatsDto.builder()
                    .reason(reason)
                    .count(count != null ? count : 0)
                    .build());
        }
        // 모든 사유 타입 포함 (0건인 것도)
        for (WithdrawalReasonType type : WithdrawalReasonType.values()) {
            if (result.stream().noneMatch(r -> r.getReason() == type)) {
                result.add(WithdrawalReasonStatsDto.builder().reason(type).count(0).build());
            }
        }
        result.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return result;
    }

    @Transactional(readOnly = true)
    public StatisticsSummaryDto getSummary() {
        long totalUserCount = userRepository.count();
        long activeUserCount = userRepository.countByDeletedAtIsNull();
        long withdrawnUserCount = totalUserCount - activeUserCount;
        long totalProductCount = productRepository.count();
        long activeProductCount = productRepository.countByDeletedAtIsNull();

        return StatisticsSummaryDto.builder()
                .totalUserCount(totalUserCount)
                .activeUserCount(activeUserCount)
                .withdrawnUserCount(withdrawnUserCount)
                .totalProductCount(totalProductCount)
                .activeProductCount(activeProductCount)
                .build();
    }
}
