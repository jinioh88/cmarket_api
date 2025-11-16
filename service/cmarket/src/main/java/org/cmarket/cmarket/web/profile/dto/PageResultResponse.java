package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 페이지네이션 결과 웹 DTO
 * 
 * 웹 계층에서 사용하는 페이지네이션 결과 DTO입니다.
 */
@Getter
@Builder
public class PageResultResponse<T> {
    private int page;
    private int size;
    private long total;
    private List<T> content;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements;
    private long numberOfElements;
    
    /**
     * 앱 DTO에서 웹 DTO로 변환
     * 
     * @param pageResult 앱 DTO
     * @return 웹 DTO
     */
    public static <T> PageResultResponse<T> fromPageResult(org.cmarket.cmarket.domain.profile.app.dto.PageResult<T> pageResult) {
        return PageResultResponse.<T>builder()
                .page(pageResult.page())
                .size(pageResult.size())
                .total(pageResult.total())
                .content(pageResult.content())
                .totalPages(pageResult.totalPages())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .totalElements(pageResult.totalElements())
                .numberOfElements(pageResult.numberOfElements())
                .build();
    }
}

