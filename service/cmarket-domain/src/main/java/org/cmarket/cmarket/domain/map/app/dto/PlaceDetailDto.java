package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@Builder
public class PlaceDetailDto {

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
    private ReviewSummaryDto reviewSummary;
    private HospitalDetailInfoDto detail;
}
