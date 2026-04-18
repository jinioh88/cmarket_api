package org.cmarket.cmarket.web.map.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.GeocodingSyncItemDto;
import org.cmarket.cmarket.domain.map.app.dto.GeocodingSyncResultDto;
import org.cmarket.cmarket.domain.map.app.service.MapAdminService;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeocodingSyncService {

    private static final double DEFAULT_THRESHOLD = 0.0001d;

    private final PlaceRepository placeRepository;
    private final NaverGeocodingClient naverGeocodingClient;
    private final MapAdminService mapAdminService;

    public GeocodingSyncResultDto syncGeocoding(
            PlaceCategory category,
            Double threshold,
            Boolean dryRun,
            Integer limit
    ) {
        double actualThreshold = threshold != null ? threshold : DEFAULT_THRESHOLD;
        boolean actualDryRun = Boolean.TRUE.equals(dryRun);

        List<Place> places = loadPlaces(category, limit);

        int totalChecked = 0;
        int updatedCount = 0;
        int matchedCount = 0;
        int failedCount = 0;
        List<Long> failedPlaceIds = new ArrayList<>();
        List<GeocodingSyncItemDto> updatedPlaces = new ArrayList<>();

        for (Place place : places) {
            totalChecked++;

            if (!StringUtils.hasText(place.getAddress())) {
                failedCount++;
                failedPlaceIds.add(place.getId());
                continue;
            }

            double[] geocoded = naverGeocodingClient.geocode(place.getAddress());
            if (geocoded == null) {
                failedCount++;
                failedPlaceIds.add(place.getId());
                continue;
            }

            double naverLongitude = geocoded[0];
            double naverLatitude = geocoded[1];

            Double currentLatitude = place.getLatitude();
            Double currentLongitude = place.getLongitude();

            if (isCoordinateDifferent(currentLatitude, naverLatitude, actualThreshold)
                    || isCoordinateDifferent(currentLongitude, naverLongitude, actualThreshold)) {
                if (!actualDryRun) {
                    mapAdminService.updateCoordinates(place.getId(), naverLatitude, naverLongitude);
                }
                updatedPlaces.add(GeocodingSyncItemDto.builder()
                        .placeId(place.getId())
                        .name(place.getName())
                        .address(place.getAddress())
                        .oldLatitude(currentLatitude)
                        .oldLongitude(currentLongitude)
                        .newLatitude(naverLatitude)
                        .newLongitude(naverLongitude)
                        .build());
                updatedCount++;
            } else {
                matchedCount++;
            }
        }

        return GeocodingSyncResultDto.builder()
                .totalChecked(totalChecked)
                .updatedCount(updatedCount)
                .matchedCount(matchedCount)
                .failedCount(failedCount)
                .dryRun(actualDryRun)
                .failedPlaceIds(failedPlaceIds)
                .updatedPlaces(updatedPlaces)
                .build();
    }

    private List<Place> loadPlaces(PlaceCategory category, Integer limit) {
        List<Place> places = placeRepository.findAll();
        if (category != null) {
            places = places.stream()
                    .filter(place -> place.getCategory() == category)
                    .sorted(Comparator.comparing(Place::getId))
                    .toList();
        } else {
            places = places.stream()
                    .sorted(Comparator.comparing(Place::getId))
                    .toList();
        }
        if (limit != null && limit > 0 && places.size() > limit) {
            return places.subList(0, limit);
        }
        return places;
    }

    private boolean isCoordinateDifferent(Double current, double candidate, double threshold) {
        if (current == null) {
            return true;
        }
        return Math.abs(current - candidate) > threshold;
    }
}
