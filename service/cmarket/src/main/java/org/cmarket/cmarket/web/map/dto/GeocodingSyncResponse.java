package org.cmarket.cmarket.web.map.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.app.dto.GeocodingSyncItemDto;
import org.cmarket.cmarket.domain.map.app.dto.GeocodingSyncResultDto;

import java.util.List;

@Getter
@Builder
public class GeocodingSyncResponse {

    private Integer totalChecked;
    private Integer updatedCount;
    private Integer matchedCount;
    private Integer failedCount;
    private Boolean dryRun;
    private List<Long> failedPlaceIds;
    private List<Item> updatedPlaces;

    public static GeocodingSyncResponse fromDto(GeocodingSyncResultDto dto) {
        return GeocodingSyncResponse.builder()
                .totalChecked(dto.getTotalChecked())
                .updatedCount(dto.getUpdatedCount())
                .matchedCount(dto.getMatchedCount())
                .failedCount(dto.getFailedCount())
                .dryRun(dto.getDryRun())
                .failedPlaceIds(dto.getFailedPlaceIds())
                .updatedPlaces(dto.getUpdatedPlaces().stream().map(Item::fromDto).toList())
                .build();
    }

    @Getter
    @Builder
    public static class Item {
        private Long placeId;
        private String name;
        private String address;
        private Double oldLatitude;
        private Double oldLongitude;
        private Double newLatitude;
        private Double newLongitude;

        public static Item fromDto(GeocodingSyncItemDto dto) {
            return Item.builder()
                    .placeId(dto.getPlaceId())
                    .name(dto.getName())
                    .address(dto.getAddress())
                    .oldLatitude(dto.getOldLatitude())
                    .oldLongitude(dto.getOldLongitude())
                    .newLatitude(dto.getNewLatitude())
                    .newLongitude(dto.getNewLongitude())
                    .build();
        }
    }
}
