package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@Setter
@NoArgsConstructor
public class PetFriendlyPlaceImportRequest {

    private PlaceCategory category = PlaceCategory.RESTAURANT;
    private Integer startPage = 1;
    private Integer endPage = 10;
    private Integer numOfRows = 100;
}
