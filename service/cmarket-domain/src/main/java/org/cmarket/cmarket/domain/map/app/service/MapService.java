package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.PlaceDetailDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceSearchCommand;
import org.cmarket.cmarket.domain.map.app.dto.PlaceSearchResultDto;

public interface MapService {

    PlaceSearchResultDto searchPlaces(PlaceSearchCommand command);

    PlaceDetailDto getPlaceDetail(Long placeId);
}
