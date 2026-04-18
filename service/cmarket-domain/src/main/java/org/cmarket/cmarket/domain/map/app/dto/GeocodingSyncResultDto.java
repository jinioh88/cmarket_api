package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GeocodingSyncResultDto {

    private Integer totalChecked;
    private Integer updatedCount;
    private Integer matchedCount;
    private Integer failedCount;
    private Boolean dryRun;
    private List<Long> failedPlaceIds;
    private List<GeocodingSyncItemDto> updatedPlaces;
}
