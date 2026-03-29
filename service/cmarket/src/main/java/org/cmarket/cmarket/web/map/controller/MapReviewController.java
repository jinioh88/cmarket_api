package org.cmarket.cmarket.web.map.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.map.app.service.MapReviewService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.map.dto.PlaceReviewCreateRequest;
import org.cmarket.cmarket.web.map.dto.PlaceReviewListResponse;
import org.cmarket.cmarket.web.map.dto.PlaceReviewResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/{placeId}/reviews")
public class MapReviewController {

    private final MapReviewService mapReviewService;

    @GetMapping
    public ResponseEntity<SuccessResponse<PlaceReviewListResponse>> getPlaceReviews(
            @PathVariable Long placeId,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        PlaceReviewListResponse response = PlaceReviewListResponse.fromDto(
                mapReviewService.getPlaceReviews(placeId, sort, page, size)
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<PlaceReviewResponse>> createPlaceReview(
            @PathVariable Long placeId,
            @Valid @RequestBody PlaceReviewCreateRequest request
    ) {
        String email = SecurityUtils.getCurrentUserEmail();
        PlaceReviewResponse response = PlaceReviewResponse.fromDto(
                mapReviewService.createPlaceReview(placeId, email, request.toCommand())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
}
