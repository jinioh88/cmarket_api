package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceCommand;
import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceRecommendationUpdateCommand;

public interface MapAdminService {

    AdminPlaceDto createPlace(AdminPlaceCommand command);

    AdminPlaceDto updatePlace(Long placeId, AdminPlaceCommand command);

    AdminPlaceDto updateRecommendation(Long placeId, PlaceRecommendationUpdateCommand command);
}
