package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@Builder
public class AdminPlaceDto {

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
    private HospitalDetailInfoDto detail;

    public static AdminPlaceDto of(Place place, HospitalDetailInfoDto detail) {
        return AdminPlaceDto.builder()
                .id(place.getId())
                .category(place.getCategory())
                .name(place.getName())
                .address(place.getAddress())
                .phone(place.getPhone())
                .operatingHours(place.getOperatingHours())
                .imageUrl(place.getImageUrl())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .isRecommended(place.getIsRecommended())
                .detail(detail)
                .build();
    }
}
