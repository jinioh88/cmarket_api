package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.HospitalImportCommand;
import org.cmarket.cmarket.domain.map.app.dto.HospitalImportResultDto;
import org.cmarket.cmarket.domain.map.app.dto.PetFriendlyPlaceImportCommand;
import org.cmarket.cmarket.domain.map.model.HospitalDetail;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.model.PlaceSourceType;
import org.cmarket.cmarket.domain.map.repository.HospitalDetailRepository;
import org.cmarket.cmarket.domain.map.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MapImportServiceImpl implements MapImportService {

    private final PlaceRepository placeRepository;
    private final HospitalDetailRepository hospitalDetailRepository;

    public MapImportServiceImpl(
            PlaceRepository placeRepository,
            HospitalDetailRepository hospitalDetailRepository
    ) {
        this.placeRepository = placeRepository;
        this.hospitalDetailRepository = hospitalDetailRepository;
    }

    @Override
    @Transactional
    public HospitalImportResultDto importHospitals(List<HospitalImportCommand> commands) {
        int importedCount = 0;
        int skippedCount = 0;

        for (HospitalImportCommand command : commands) {
            if (command.getLatitude() == null || command.getLongitude() == null) {
                skippedCount++;
                continue;
            }

            Place place = placeRepository.findByExternalPlaceId(command.getExternalPlaceId()).orElse(null);
            if (place == null) {
                place = Place.builder()
                        .category(PlaceCategory.HOSPITAL)
                        .name(command.getName())
                        .address(command.getAddress())
                        .phone(command.getPhone())
                        .operatingHours(null)
                        .imageUrl(null)
                        .latitude(command.getLatitude())
                        .longitude(command.getLongitude())
                        .isRecommended(false)
                        .sourceType(PlaceSourceType.PUBLIC_DATA)
                        .externalPlaceId(command.getExternalPlaceId())
                        .salesStatusCode(command.getSalesStatusCode())
                        .salesStatusName(command.getSalesStatusName())
                        .build();
                place = placeRepository.save(place);
            } else {
                place.update(
                        PlaceCategory.HOSPITAL,
                        command.getName(),
                        command.getAddress(),
                        command.getPhone(),
                        null,
                        null,
                        command.getLatitude(),
                        command.getLongitude(),
                        place.getIsRecommended(),
                        PlaceSourceType.PUBLIC_DATA,
                        command.getExternalPlaceId(),
                        command.getSalesStatusCode(),
                        command.getSalesStatusName()
                );
            }

            HospitalDetail hospitalDetail = hospitalDetailRepository.findByPlaceId(place.getId()).orElse(null);
            if (hospitalDetail == null) {
                hospitalDetail = HospitalDetail.builder()
                        .placeId(place.getId())
                        .is24Hours(false)
                        .isEmergencyAvailable(false)
                        .animalTypes(command.getAnimalTypes())
                        .build();
                hospitalDetailRepository.save(hospitalDetail);
            } else {
                hospitalDetail.update(
                        hospitalDetail.getIs24Hours(),
                        hospitalDetail.getIsEmergencyAvailable(),
                        command.getAnimalTypes()
                );
            }

            importedCount++;
        }

        return HospitalImportResultDto.builder()
                .requestedCount(commands.size())
                .importedCount(importedCount)
                .skippedCount(skippedCount)
                .build();
    }

    @Override
    @Transactional
    public HospitalImportResultDto importPetFriendlyPlaces(List<PetFriendlyPlaceImportCommand> commands) {
        int importedCount = 0;
        int skippedCount = 0;

        for (PetFriendlyPlaceImportCommand command : commands) {
            if (command.getLatitude() == null || command.getLongitude() == null) {
                skippedCount++;
                continue;
            }

            Place place = placeRepository.findByExternalPlaceId(command.getExternalPlaceId()).orElse(null);
            if (place == null) {
                place = Place.builder()
                        .category(command.getCategory())
                        .name(command.getName())
                        .address(command.getAddress())
                        .phone(command.getPhone())
                        .operatingHours(null)
                        .imageUrl(command.getImageUrl())
                        .latitude(command.getLatitude())
                        .longitude(command.getLongitude())
                        .isRecommended(false)
                        .sourceType(PlaceSourceType.PUBLIC_DATA)
                        .externalPlaceId(command.getExternalPlaceId())
                        .salesStatusCode(command.getSalesStatusCode())
                        .salesStatusName(command.getSalesStatusName())
                        .build();
                placeRepository.save(place);
            } else {
                place.update(
                        command.getCategory(),
                        command.getName(),
                        command.getAddress(),
                        command.getPhone(),
                        null,
                        command.getImageUrl(),
                        command.getLatitude(),
                        command.getLongitude(),
                        place.getIsRecommended(),
                        PlaceSourceType.PUBLIC_DATA,
                        command.getExternalPlaceId(),
                        command.getSalesStatusCode(),
                        command.getSalesStatusName()
                );
            }

            importedCount++;
        }

        return HospitalImportResultDto.builder()
                .requestedCount(commands.size())
                .importedCount(importedCount)
                .skippedCount(skippedCount)
                .build();
    }
}
