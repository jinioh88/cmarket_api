package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalListItemDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

/**
 * 탈퇴 회원 목록 응답 DTO
 */
@Getter
@Builder
public class WithdrawalListResponse {
    private List<WithdrawalListItemResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static WithdrawalListResponse fromPageResult(PageResult<WithdrawalListItemDto> pageResult) {
        List<WithdrawalListItemResponse> content = pageResult.content().stream()
                .map(WithdrawalListItemResponse::fromDto)
                .toList();

        return WithdrawalListResponse.builder()
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
