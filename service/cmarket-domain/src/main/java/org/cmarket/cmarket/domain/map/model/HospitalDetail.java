package org.cmarket.cmarket.domain.map.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 병원 전용 상세 정보
 */
@Entity
@Table(name = "hospital_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HospitalDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "place_id", unique = true)
    private Long placeId;

    @Column(nullable = false, name = "is_24_hours")
    private Boolean is24Hours;

    @Column(nullable = false, name = "is_emergency_available")
    private Boolean isEmergencyAvailable;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "hospital_detail_animal_types",
            joinColumns = @JoinColumn(name = "hospital_detail_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type", length = 30)
    private List<AnimalType> animalTypes = new ArrayList<>();

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public HospitalDetail(
            Long placeId,
            Boolean is24Hours,
            Boolean isEmergencyAvailable,
            List<AnimalType> animalTypes
    ) {
        this.placeId = placeId;
        this.is24Hours = is24Hours != null ? is24Hours : Boolean.FALSE;
        this.isEmergencyAvailable = isEmergencyAvailable != null ? isEmergencyAvailable : Boolean.FALSE;
        this.animalTypes = animalTypes != null ? new ArrayList<>(animalTypes) : new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
            Boolean is24Hours,
            Boolean isEmergencyAvailable,
            List<AnimalType> animalTypes
    ) {
        this.is24Hours = is24Hours != null ? is24Hours : Boolean.FALSE;
        this.isEmergencyAvailable = isEmergencyAvailable != null ? isEmergencyAvailable : Boolean.FALSE;
        this.animalTypes = animalTypes != null ? new ArrayList<>(animalTypes) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }
}
