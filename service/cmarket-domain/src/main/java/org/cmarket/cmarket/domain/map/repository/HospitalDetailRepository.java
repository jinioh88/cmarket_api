package org.cmarket.cmarket.domain.map.repository;

import org.cmarket.cmarket.domain.map.model.HospitalDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HospitalDetailRepository extends JpaRepository<HospitalDetail, Long> {

    Optional<HospitalDetail> findByPlaceId(Long placeId);

    List<HospitalDetail> findByPlaceIdIn(List<Long> placeIds);
}
