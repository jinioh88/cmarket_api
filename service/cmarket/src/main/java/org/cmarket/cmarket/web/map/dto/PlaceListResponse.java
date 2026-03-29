package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.PlaceSearchResultDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaceListResponse {

    private int page;
    private int size;
    private long total;
    private List<PlaceListItemResponse> items;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements;
    private long numberOfElements;

    public static PlaceListResponse fromDto(PlaceSearchResultDto dto) {
        PlaceListResponse response = new PlaceListResponse();
        PageResult<org.cmarket.cmarket.domain.map.app.dto.PlaceListItemDto> pageResult = dto.places();

        response.page = pageResult.page();
        response.size = pageResult.size();
        response.total = pageResult.total();
        response.items = pageResult.content().stream()
                .map(PlaceListItemResponse::fromDto)
                .toList();
        response.totalPages = pageResult.totalPages();
        response.hasNext = pageResult.hasNext();
        response.hasPrevious = pageResult.hasPrevious();
        response.totalElements = pageResult.totalElements();
        response.numberOfElements = pageResult.numberOfElements();
        return response;
    }
}
