package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

/**
 * 어드민 유저 목록 응답 DTO
 */
@Getter
@Builder
public class AdminUserListResponse {
    private List<AdminUserListItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static AdminUserListResponse fromPageResult(PageResult<org.cmarket.cmarket.domain.admin.app.dto.AdminUserListItemDto> pageResult) {
        List<AdminUserListItemResponse> content = pageResult.content().stream()
                .map(AdminUserListItemResponse::fromDto)
                .toList();

        return AdminUserListResponse.builder()
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
