package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewCreateCommand;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewListDto;

public interface MapReviewService {

    PlaceReviewListDto getPlaceReviews(Long placeId, String sort, Integer page, Integer size);

    PlaceReviewDto createPlaceReview(Long placeId, String email, PlaceReviewCreateCommand command);
}
