package org.cmarket.cmarket.domain.map.repository;

import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlaceRepositoryCustom {

    Page<Place> searchPlaces(
            PlaceCategory category,
            Double latitude,
            Double longitude,
            Double radius,
            Boolean isRecommended,
            Boolean is24Hours,
            Boolean isEmergencyAvailable,
            List<AnimalType> animalTypes,
            Pageable pageable
    );

    Page<Place> searchAdminPlaces(
            String keyword,
            PlaceCategory category,
            Boolean isRecommended,
            Pageable pageable
    );
}
