package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceListDto;
import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceListItemDto;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.repository.PlaceRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapAdminQueryServiceImpl implements MapAdminQueryService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final PlaceRepository placeRepository;

    public MapAdminQueryServiceImpl(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPlaceListDto getPlacesForAdmin(
            String keyword,
            PlaceCategory category,
            Boolean isRecommended,
            Integer page,
            Integer size
    ) {
        Pageable pageable = createPageable(page, size);
        Page<Place> placePage = placeRepository.searchAdminPlaces(keyword, category, isRecommended, pageable);
        PageResult<AdminPlaceListItemDto> pageResult = PageResult.fromPage(placePage.map(AdminPlaceListItemDto::fromEntity));
        return new AdminPlaceListDto(pageResult);
    }

    private Pageable createPageable(Integer page, Integer size) {
        int pageNumber = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        return PageRequest.of(pageNumber, pageSize);
    }
}
