package org.cmarket.cmarket.domain.map.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 장소 리뷰 엔티티
 */
@Entity
@Table(name = "place_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "place_id")
    private Long placeId;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "place_review_images",
            joinColumns = @JoinColumn(name = "place_review_id")
    )
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public PlaceReview(
            Long placeId,
            Long userId,
            String nickname,
            Integer rating,
            String content,
            List<String> imageUrls
    ) {
        this.placeId = placeId;
        this.userId = userId;
        this.nickname = nickname;
        this.rating = rating;
        this.content = content;
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
