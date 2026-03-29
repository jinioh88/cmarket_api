package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.PlaceReview;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class PlaceReviewDto {

    private Long id;
    private String nickname;
    private Integer rating;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static PlaceReviewDto fromEntity(PlaceReview placeReview) {
        return PlaceReviewDto.builder()
                .id(placeReview.getId())
                .nickname(placeReview.getNickname())
                .rating(placeReview.getRating())
                .content(placeReview.getContent())
                .imageUrls(new ArrayList<>(placeReview.getImageUrls()))
                .createdAt(placeReview.getCreatedAt())
                .build();
    }
}
