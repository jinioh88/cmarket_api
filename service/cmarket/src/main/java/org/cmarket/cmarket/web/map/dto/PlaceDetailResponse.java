package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.PlaceDetailDto;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@NoArgsConstructor
public class PlaceDetailResponse {

    private Long id;
    private PlaceCategory category;
    private String name;
    private String address;
    private String phone;
    private String operatingHours;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private Boolean isRecommended;
    private ReviewSummaryResponse reviewSummary;
    private HospitalDetailInfoResponse detail;

    public static PlaceDetailResponse fromDto(PlaceDetailDto dto) {
        PlaceDetailResponse response = new PlaceDetailResponse();
        response.id = dto.getId();
        response.category = dto.getCategory();
        response.name = dto.getName();
        response.address = dto.getAddress();
        response.phone = dto.getPhone();
        response.operatingHours = dto.getOperatingHours();
        response.imageUrl = dto.getImageUrl();
        response.latitude = dto.getLatitude();
        response.longitude = dto.getLongitude();
        response.isRecommended = dto.getIsRecommended();
        response.reviewSummary = ReviewSummaryResponse.fromDto(dto.getReviewSummary());
        response.detail = HospitalDetailInfoResponse.fromDto(dto.getDetail());
        return response;
    }
}
