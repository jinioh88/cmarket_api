package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeocodingSyncItemDto {

    private Long placeId;
    private String name;
    private String address;
    private Double oldLatitude;
    private Double oldLongitude;
    private Double newLatitude;
    private Double newLongitude;
}
