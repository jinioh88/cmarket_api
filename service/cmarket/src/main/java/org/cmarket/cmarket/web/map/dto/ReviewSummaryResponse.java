package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.ReviewSummaryDto;

@Getter
@NoArgsConstructor
public class ReviewSummaryResponse {

    private Long reviewCount;
    private Double averageRating;

    public static ReviewSummaryResponse fromDto(ReviewSummaryDto dto) {
        ReviewSummaryResponse response = new ReviewSummaryResponse();
        response.reviewCount = dto.getReviewCount();
        response.averageRating = dto.getAverageRating();
        return response;
    }
}
