package org.cmarket.cmarket.web.map.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.PlaceSearchCommand;
import org.cmarket.cmarket.domain.map.app.service.MapService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.map.dto.PlaceDetailResponse;
import org.cmarket.cmarket.web.map.dto.PlaceListResponse;
import org.cmarket.cmarket.web.map.dto.PlaceSearchRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class MapController {

    private final MapService mapService;

    @GetMapping
    public ResponseEntity<SuccessResponse<PlaceListResponse>> searchPlaces(
            @Valid @ModelAttribute PlaceSearchRequest request
    ) {
        PlaceSearchCommand command = PlaceSearchCommand.builder()
                .category(request.getCategory())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radius(request.getRadius())
                .minLatitude(request.getMinLatitude())
                .maxLatitude(request.getMaxLatitude())
                .minLongitude(request.getMinLongitude())
                .maxLongitude(request.getMaxLongitude())
                .isRecommended(request.getIsRecommended())
                .is24Hours(request.getIs24Hours())
                .isEmergencyAvailable(request.getIsEmergencyAvailable())
                .animalTypes(request.getAnimalTypes())
                .page(request.getPage())
                .size(request.getSize())
                .build();

        PlaceListResponse response = PlaceListResponse.fromDto(mapService.searchPlaces(command));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<SuccessResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable Long placeId
    ) {
        PlaceDetailResponse response = PlaceDetailResponse.fromDto(mapService.getPlaceDetail(placeId));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}
