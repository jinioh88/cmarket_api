package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.HospitalDetailInfoDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceDetailDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceListItemDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceSearchCommand;
import org.cmarket.cmarket.domain.map.app.dto.PlaceSearchResultDto;
import org.cmarket.cmarket.domain.map.app.dto.ReviewSummaryDto;
import org.cmarket.cmarket.domain.map.app.exception.InvalidLocationRangeException;
import org.cmarket.cmarket.domain.map.app.exception.InvalidPlaceFilterException;
import org.cmarket.cmarket.domain.map.app.exception.PlaceNotFoundException;
import org.cmarket.cmarket.domain.map.model.HospitalDetail;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.repository.HospitalDetailRepository;
import org.cmarket.cmarket.domain.map.repository.PlaceRepository;
import org.cmarket.cmarket.domain.map.repository.PlaceReviewRepository;
import org.cmarket.cmarket.domain.map.repository.PlaceReviewSummaryProjection;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MapServiceImpl implements MapService {

    private static final double DEFAULT_MAX_RADIUS_KM = 30.0;
    private static final double DEFAULT_LATITUDE = 37.5666;
    private static final double DEFAULT_LONGITUDE = 126.9784;
    private static final double DEFAULT_RADIUS_KM = 3.0;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final PlaceRepository placeRepository;
    private final HospitalDetailRepository hospitalDetailRepository;
    private final PlaceReviewRepository placeReviewRepository;

    public MapServiceImpl(
            PlaceRepository placeRepository,
            HospitalDetailRepository hospitalDetailRepository,
            PlaceReviewRepository placeReviewRepository
    ) {
        this.placeRepository = placeRepository;
        this.hospitalDetailRepository = hospitalDetailRepository;
        this.placeReviewRepository = placeReviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceSearchResultDto searchPlaces(PlaceSearchCommand command) {
        validateCommand(command);

        Pageable pageable = createPageable(command.getPage(), command.getSize());
        Page<Place> placePage = placeRepository.searchPlaces(
                command.getCategory(),
                getLatitudeOrDefault(command.getLatitude(), command.getLongitude()),
                getLongitudeOrDefault(command.getLatitude(), command.getLongitude()),
                getRadiusOrDefault(command.getRadius()),
                command.getMinLatitude(),
                command.getMaxLatitude(),
                command.getMinLongitude(),
                command.getMaxLongitude(),
                command.getIsRecommended(),
                command.getIs24Hours(),
                command.getIsEmergencyAvailable(),
                command.getAnimalTypes(),
                pageable
        );

        List<Long> placeIds = placePage.getContent().stream()
                .map(Place::getId)
                .toList();

        Map<Long, HospitalDetail> hospitalDetailMap = getHospitalDetailMap(placeIds);
        Map<Long, ReviewSummaryDto> reviewSummaryMap = getReviewSummaryMap(placeIds);

        PageResult<PlaceListItemDto> pageResult = PageResult.fromPage(
                placePage.map(place -> PlaceListItemDto.builder()
                        .id(place.getId())
                        .category(place.getCategory())
                        .name(place.getName())
                        .imageUrl(place.getImageUrl())
                        .latitude(place.getLatitude())
                        .longitude(place.getLongitude())
                        .isRecommended(place.getIsRecommended())
                        .reviewSummary(reviewSummaryMap.getOrDefault(place.getId(), ReviewSummaryDto.empty()))
                        .detail(HospitalDetailInfoDto.fromEntity(hospitalDetailMap.get(place.getId())))
                        .build())
        );

        return new PlaceSearchResultDto(pageResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceDetailDto getPlaceDetail(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(PlaceNotFoundException::new);

        HospitalDetail hospitalDetail = null;
        if (place.getCategory() == PlaceCategory.HOSPITAL) {
            hospitalDetail = hospitalDetailRepository.findByPlaceId(placeId).orElse(null);
        }

        ReviewSummaryDto reviewSummary = getReviewSummaryMap(Collections.singletonList(placeId))
                .getOrDefault(placeId, ReviewSummaryDto.empty());

        return PlaceDetailDto.builder()
                .id(place.getId())
                .category(place.getCategory())
                .name(place.getName())
                .address(place.getAddress())
                .phone(place.getPhone())
                .operatingHours(place.getOperatingHours())
                .imageUrl(place.getImageUrl())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .isRecommended(place.getIsRecommended())
                .reviewSummary(reviewSummary)
                .detail(HospitalDetailInfoDto.fromEntity(hospitalDetail))
                .build();
    }

    private void validateCommand(PlaceSearchCommand command) {
        if (command.getCategory() == null) {
            throw new InvalidPlaceFilterException("카테고리는 필수입니다.");
        }
        boolean hasBounds = hasBounds(command);

        if (hasBounds) {
            validateBounds(command);
        } else {
            validateCoordinates(command.getLatitude(), command.getLongitude());
            if (command.getRadius() != null
                    && (command.getRadius() <= 0 || command.getRadius() > DEFAULT_MAX_RADIUS_KM)) {
                throw new InvalidLocationRangeException("반경은 0보다 크고 " + DEFAULT_MAX_RADIUS_KM + "km 이하여야 합니다.");
            }
        }

        boolean hasHospitalOnlyFilter = command.getIs24Hours() != null
                || command.getIsEmergencyAvailable() != null
                || (command.getAnimalTypes() != null && !command.getAnimalTypes().isEmpty());

        if (command.getCategory() != PlaceCategory.HOSPITAL && hasHospitalOnlyFilter) {
            throw new InvalidPlaceFilterException("병원 전용 필터는 HOSPITAL 카테고리에서만 사용할 수 있습니다.");
        }
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return;
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidLocationRangeException("위도 값이 올바르지 않습니다.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidLocationRangeException("경도 값이 올바르지 않습니다.");
        }
    }

    private boolean hasBounds(PlaceSearchCommand command) {
        return command.getMinLatitude() != null
                || command.getMaxLatitude() != null
                || command.getMinLongitude() != null
                || command.getMaxLongitude() != null;
    }

    private void validateBounds(PlaceSearchCommand command) {
        if (command.getMinLatitude() == null
                || command.getMaxLatitude() == null
                || command.getMinLongitude() == null
                || command.getMaxLongitude() == null) {
            throw new InvalidLocationRangeException("지도 영역 조회에는 min/max latitude, longitude가 모두 필요합니다.");
        }

        validateCoordinates(command.getMinLatitude(), command.getMinLongitude());
        validateCoordinates(command.getMaxLatitude(), command.getMaxLongitude());

        if (command.getMinLatitude() > command.getMaxLatitude()) {
            throw new InvalidLocationRangeException("minLatitude는 maxLatitude보다 클 수 없습니다.");
        }
        if (command.getMinLongitude() > command.getMaxLongitude()) {
            throw new InvalidLocationRangeException("minLongitude는 maxLongitude보다 클 수 없습니다.");
        }
    }

    private Pageable createPageable(Integer page, Integer size) {
        int pageNumber = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        return PageRequest.of(pageNumber, pageSize);
    }

    private Double getLatitudeOrDefault(Double latitude, Double longitude) {
        return latitude != null && longitude != null ? latitude : DEFAULT_LATITUDE;
    }

    private Double getLongitudeOrDefault(Double latitude, Double longitude) {
        return latitude != null && longitude != null ? longitude : DEFAULT_LONGITUDE;
    }

    private Double getRadiusOrDefault(Double radius) {
        return radius != null ? radius : DEFAULT_RADIUS_KM;
    }

    private Map<Long, HospitalDetail> getHospitalDetailMap(List<Long> placeIds) {
        if (placeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return hospitalDetailRepository.findByPlaceIdIn(placeIds).stream()
                .collect(Collectors.toMap(HospitalDetail::getPlaceId, Function.identity()));
    }

    private Map<Long, ReviewSummaryDto> getReviewSummaryMap(List<Long> placeIds) {
        if (placeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return placeReviewRepository.findReviewSummariesByPlaceIds(placeIds).stream()
                .collect(Collectors.toMap(
                        PlaceReviewSummaryProjection::getPlaceId,
                        projection -> ReviewSummaryDto.of(
                                projection.getReviewCount(),
                                projection.getAverageRating()
                        )
                ));
    }
}
