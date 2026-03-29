package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceCommand;
import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceDto;
import org.cmarket.cmarket.domain.map.app.dto.HospitalDetailInfoDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceRecommendationUpdateCommand;
import org.cmarket.cmarket.domain.map.app.exception.PlaceNotFoundException;
import org.cmarket.cmarket.domain.map.model.HospitalDetail;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.model.PlaceSourceType;
import org.cmarket.cmarket.domain.map.repository.HospitalDetailRepository;
import org.cmarket.cmarket.domain.map.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapAdminServiceImpl implements MapAdminService {

    private final PlaceRepository placeRepository;
    private final HospitalDetailRepository hospitalDetailRepository;

    public MapAdminServiceImpl(
            PlaceRepository placeRepository,
            HospitalDetailRepository hospitalDetailRepository
    ) {
        this.placeRepository = placeRepository;
        this.hospitalDetailRepository = hospitalDetailRepository;
    }

    @Override
    @Transactional
    public AdminPlaceDto createPlace(AdminPlaceCommand command) {
        Place place = Place.builder()
                .category(command.getCategory())
                .name(command.getName())
                .address(command.getAddress())
                .phone(command.getPhone())
                .operatingHours(command.getOperatingHours())
                .imageUrl(command.getImageUrl())
                .latitude(command.getLatitude())
                .longitude(command.getLongitude())
                .isRecommended(command.getIsRecommended())
                .sourceType(PlaceSourceType.ADMIN)
                .externalPlaceId(null)
                .build();

        Place savedPlace = placeRepository.save(place);
        HospitalDetail hospitalDetail = upsertHospitalDetail(savedPlace.getId(), command);
        return AdminPlaceDto.of(savedPlace, HospitalDetailInfoDto.fromEntity(hospitalDetail));
    }

    @Override
    @Transactional
    public AdminPlaceDto updatePlace(Long placeId, AdminPlaceCommand command) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(PlaceNotFoundException::new);

        place.update(
                command.getCategory(),
                command.getName(),
                command.getAddress(),
                command.getPhone(),
                command.getOperatingHours(),
                command.getImageUrl(),
                command.getLatitude(),
                command.getLongitude(),
                command.getIsRecommended(),
                PlaceSourceType.ADMIN,
                null
        );

        HospitalDetail hospitalDetail = upsertHospitalDetail(placeId, command);
        return AdminPlaceDto.of(place, HospitalDetailInfoDto.fromEntity(hospitalDetail));
    }

    @Override
    @Transactional
    public AdminPlaceDto updateRecommendation(Long placeId, PlaceRecommendationUpdateCommand command) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(PlaceNotFoundException::new);

        place.updateRecommendation(command.getIsRecommended());
        HospitalDetail hospitalDetail = hospitalDetailRepository.findByPlaceId(placeId).orElse(null);
        return AdminPlaceDto.of(place, HospitalDetailInfoDto.fromEntity(hospitalDetail));
    }

    private HospitalDetail upsertHospitalDetail(Long placeId, AdminPlaceCommand command) {
        if (command.getCategory() != PlaceCategory.HOSPITAL) {
            return null;
        }

        HospitalDetail hospitalDetail = hospitalDetailRepository.findByPlaceId(placeId).orElse(null);
        if (hospitalDetail == null) {
            hospitalDetail = HospitalDetail.builder()
                    .placeId(placeId)
                    .is24Hours(command.getIs24Hours())
                    .isEmergencyAvailable(command.getIsEmergencyAvailable())
                    .animalTypes(command.getAnimalTypes())
                    .build();
            return hospitalDetailRepository.save(hospitalDetail);
        }

        hospitalDetail.update(
                command.getIs24Hours(),
                command.getIsEmergencyAvailable(),
                command.getAnimalTypes()
        );
        return hospitalDetail;
    }
}
