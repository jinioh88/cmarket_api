package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PlaceReviewResponse {

    private Long id;
    private String nickname;
    private Integer rating;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    public static PlaceReviewResponse fromDto(PlaceReviewDto dto) {
        PlaceReviewResponse response = new PlaceReviewResponse();
        response.id = dto.getId();
        response.nickname = dto.getNickname();
        response.rating = dto.getRating();
        response.content = dto.getContent();
        response.imageUrls = dto.getImageUrls();
        response.createdAt = dto.getCreatedAt();
        return response;
    }
}
