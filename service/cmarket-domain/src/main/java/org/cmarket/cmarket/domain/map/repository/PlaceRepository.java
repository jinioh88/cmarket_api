package org.cmarket.cmarket.domain.map.repository;

import org.cmarket.cmarket.domain.map.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long>, PlaceRepositoryCustom {

    Optional<Place> findByExternalPlaceId(String externalPlaceId);
}
