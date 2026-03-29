package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceListItemDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

@Getter
@Builder
public class AdminPlaceListResponse {

    private List<AdminPlaceListItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static AdminPlaceListResponse fromPageResult(PageResult<AdminPlaceListItemDto> pageResult) {
        return AdminPlaceListResponse.builder()
                .content(pageResult.content().stream()
                        .map(AdminPlaceListItemResponse::fromDto)
                        .toList())
                .page(pageResult.page())
                .size(pageResult.size())
                .totalElements(pageResult.totalElements())
                .totalPages(pageResult.totalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }
}
