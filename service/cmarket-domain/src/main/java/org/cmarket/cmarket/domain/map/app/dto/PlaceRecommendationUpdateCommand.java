package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlaceRecommendationUpdateCommand {

    private Boolean isRecommended;
}
