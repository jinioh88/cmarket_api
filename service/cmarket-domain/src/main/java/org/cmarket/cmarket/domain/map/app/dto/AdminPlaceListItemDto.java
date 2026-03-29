package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.model.PlaceSourceType;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminPlaceListItemDto {

    private Long id;
    private PlaceCategory category;
    private String name;
    private String address;
    private Boolean isRecommended;
    private PlaceSourceType sourceType;
    private LocalDateTime updatedAt;

    public static AdminPlaceListItemDto fromEntity(Place place) {
        return AdminPlaceListItemDto.builder()
                .id(place.getId())
                .category(place.getCategory())
                .name(place.getName())
                .address(place.getAddress())
                .isRecommended(place.getIsRecommended())
                .sourceType(place.getSourceType())
                .updatedAt(place.getUpdatedAt())
                .build();
    }
}
