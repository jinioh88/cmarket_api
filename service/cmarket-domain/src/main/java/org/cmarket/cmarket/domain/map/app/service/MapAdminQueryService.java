package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceListDto;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

public interface MapAdminQueryService {

    AdminPlaceListDto getPlacesForAdmin(
            String keyword,
            PlaceCategory category,
            Boolean isRecommended,
            Integer page,
            Integer size
    );
}
