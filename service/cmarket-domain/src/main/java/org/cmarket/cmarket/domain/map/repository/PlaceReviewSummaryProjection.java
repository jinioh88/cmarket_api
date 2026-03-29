package org.cmarket.cmarket.domain.map.repository;

public interface PlaceReviewSummaryProjection {

    Long getPlaceId();

    Long getReviewCount();

    Double getAverageRating();
}
