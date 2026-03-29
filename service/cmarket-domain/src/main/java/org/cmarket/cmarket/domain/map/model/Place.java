package org.cmarket.cmarket.domain.map.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지도 장소 공통 엔티티
 */
@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlaceCategory category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(name = "operating_hours", length = 255)
    private String operatingHours;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, name = "is_recommended")
    private Boolean isRecommended;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "source_type", length = 20)
    private PlaceSourceType sourceType;

    @Column(name = "external_place_id", length = 100, unique = true)
    private String externalPlaceId;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Place(
            PlaceCategory category,
            String name,
            String address,
            String phone,
            String operatingHours,
            String imageUrl,
            Double latitude,
            Double longitude,
            Boolean isRecommended,
            PlaceSourceType sourceType,
            String externalPlaceId
    ) {
        this.category = category;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.operatingHours = operatingHours;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isRecommended = isRecommended != null ? isRecommended : Boolean.FALSE;
        this.sourceType = sourceType != null ? sourceType : PlaceSourceType.ADMIN;
        this.externalPlaceId = externalPlaceId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
            PlaceCategory category,
            String name,
            String address,
            String phone,
            String operatingHours,
            String imageUrl,
            Double latitude,
            Double longitude,
            Boolean isRecommended,
            PlaceSourceType sourceType,
            String externalPlaceId
    ) {
        this.category = category;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.operatingHours = operatingHours;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isRecommended = isRecommended != null ? isRecommended : Boolean.FALSE;
        this.sourceType = sourceType != null ? sourceType : this.sourceType;
        this.externalPlaceId = externalPlaceId;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRecommendation(Boolean isRecommended) {
        this.isRecommended = isRecommended != null ? isRecommended : Boolean.FALSE;
        this.updatedAt = LocalDateTime.now();
    }
}
