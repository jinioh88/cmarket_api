package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.AdminProductListItemDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

/**
 * 어드민 상품 목록 응답 DTO
 */
@Getter
@Builder
public class AdminProductListResponse {
    private List<AdminProductListItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static AdminProductListResponse fromPageResult(PageResult<AdminProductListItemDto> pageResult) {
        List<AdminProductListItemResponse> content = pageResult.content().stream()
                .map(AdminProductListItemResponse::fromDto)
                .toList();

        return AdminProductListResponse.builder()
                .content(content)
                .page(pageResult.page())
                .size(pageResult.size())
                .totalElements(pageResult.totalElements())
                .totalPages(pageResult.totalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
    }
}
