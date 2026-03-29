package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Builder
public class ReviewSummaryDto {

    private Long reviewCount;
    private Double averageRating;

    public static ReviewSummaryDto empty() {
        return ReviewSummaryDto.builder()
                .reviewCount(0L)
                .averageRating(0.0)
                .build();
    }

    public static ReviewSummaryDto of(Long reviewCount, Double averageRating) {
        double normalizedRating = averageRating != null
                ? BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return ReviewSummaryDto.builder()
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .averageRating(normalizedRating)
                .build();
    }
}
