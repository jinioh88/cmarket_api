package org.cmarket.cmarket.domain.map.repository;

import org.cmarket.cmarket.domain.map.model.PlaceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {

    Page<PlaceReview> findByPlaceIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long placeId, Pageable pageable);

    Page<PlaceReview> findByPlaceIdAndDeletedAtIsNullOrderByRatingDescCreatedAtDesc(Long placeId, Pageable pageable);

    @Query("""
            SELECT pr.placeId AS placeId,
                   COUNT(pr.id) AS reviewCount,
                   COALESCE(AVG(pr.rating), 0) AS averageRating
            FROM PlaceReview pr
            WHERE pr.deletedAt IS NULL
              AND pr.placeId IN :placeIds
            GROUP BY pr.placeId
            """)
    List<PlaceReviewSummaryProjection> findReviewSummariesByPlaceIds(@Param("placeIds") List<Long> placeIds);
}
