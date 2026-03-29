package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewCreateCommand;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewDto;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewListDto;
import org.cmarket.cmarket.domain.map.app.exception.PlaceNotFoundException;
import org.cmarket.cmarket.domain.map.model.Place;
import org.cmarket.cmarket.domain.map.model.PlaceReview;
import org.cmarket.cmarket.domain.map.repository.PlaceRepository;
import org.cmarket.cmarket.domain.map.repository.PlaceReviewRepository;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapReviewServiceImpl implements MapReviewService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final PlaceRepository placeRepository;
    private final PlaceReviewRepository placeReviewRepository;
    private final UserRepository userRepository;

    public MapReviewServiceImpl(
            PlaceRepository placeRepository,
            PlaceReviewRepository placeReviewRepository,
            UserRepository userRepository
    ) {
        this.placeRepository = placeRepository;
        this.placeReviewRepository = placeReviewRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PlaceReviewListDto getPlaceReviews(Long placeId, String sort, Integer page, Integer size) {
        ensurePlaceExists(placeId);

        Pageable pageable = createPageable(page, size);
        Page<PlaceReview> reviewPage = "rating".equalsIgnoreCase(sort)
                ? placeReviewRepository.findByPlaceIdAndDeletedAtIsNullOrderByRatingDescCreatedAtDesc(placeId, pageable)
                : placeReviewRepository.findByPlaceIdAndDeletedAtIsNullOrderByCreatedAtDesc(placeId, pageable);

        PageResult<PlaceReviewDto> pageResult = PageResult.fromPage(reviewPage.map(PlaceReviewDto::fromEntity));
        return new PlaceReviewListDto(pageResult);
    }

    @Override
    @Transactional
    public PlaceReviewDto createPlaceReview(Long placeId, String email, PlaceReviewCreateCommand command) {
        ensurePlaceExists(placeId);

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(UserNotFoundException::new);

        PlaceReview placeReview = PlaceReview.builder()
                .placeId(placeId)
                .userId(user.getId())
                .nickname(user.getNickname())
                .rating(command.getRating())
                .content(command.getContent())
                .imageUrls(command.getImageUrls())
                .build();

        PlaceReview savedReview = placeReviewRepository.save(placeReview);
        return PlaceReviewDto.fromEntity(savedReview);
    }

    private void ensurePlaceExists(Long placeId) {
        Place place = placeRepository.findById(placeId).orElse(null);
        if (place == null) {
            throw new PlaceNotFoundException();
        }
    }

    private Pageable createPageable(Integer page, Integer size) {
        int pageNumber = page != null && page >= 0 ? page : DEFAULT_PAGE;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        return PageRequest.of(pageNumber, pageSize);
    }
}
