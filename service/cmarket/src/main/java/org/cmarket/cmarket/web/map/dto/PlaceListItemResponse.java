package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.PlaceListItemDto;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@NoArgsConstructor
public class PlaceListItemResponse {

    private Long id;
    private PlaceCategory category;
    private String name;
    private String address;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private Boolean isRecommended;
    private ReviewSummaryResponse reviewSummary;
    private HospitalDetailInfoResponse detail;

    public static PlaceListItemResponse fromDto(PlaceListItemDto dto) {
        PlaceListItemResponse response = new PlaceListItemResponse();
        response.id = dto.getId();
        response.category = dto.getCategory();
        response.name = dto.getName();
        response.address = dto.getAddress();
        response.imageUrl = dto.getImageUrl();
        response.latitude = dto.getLatitude();
        response.longitude = dto.getLongitude();
        response.isRecommended = dto.getIsRecommended();
        response.reviewSummary = ReviewSummaryResponse.fromDto(dto.getReviewSummary());
        response.detail = HospitalDetailInfoResponse.fromDto(dto.getDetail());
        return response;
    }
}
