package org.cmarket.cmarket.web.map.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cmarket.cmarket.domain.map.app.dto.PlaceRecommendationUpdateCommand;

@Getter
@Setter
@NoArgsConstructor
public class PlaceRecommendationUpdateRequest {

    @NotNull(message = "isRecommended는 필수입니다.")
    private Boolean isRecommended;

    public PlaceRecommendationUpdateCommand toCommand() {
        return PlaceRecommendationUpdateCommand.builder()
                .isRecommended(isRecommended)
                .build();
    }
}
