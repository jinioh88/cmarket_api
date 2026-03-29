package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewListDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaceReviewListResponse {

    private List<PlaceReviewResponse> items;
    private int page;
    private int size;
    private long totalCount;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static PlaceReviewListResponse fromDto(PlaceReviewListDto dto) {
        PlaceReviewListResponse response = new PlaceReviewListResponse();
        PageResult<org.cmarket.cmarket.domain.map.app.dto.PlaceReviewDto> pageResult = dto.reviews();
        response.items = pageResult.content().stream()
                .map(PlaceReviewResponse::fromDto)
                .toList();
        response.page = pageResult.page();
        response.size = pageResult.size();
        response.totalCount = pageResult.totalElements();
        response.totalPages = pageResult.totalPages();
        response.hasNext = pageResult.hasNext();
        response.hasPrevious = pageResult.hasPrevious();
        return response;
    }
}
