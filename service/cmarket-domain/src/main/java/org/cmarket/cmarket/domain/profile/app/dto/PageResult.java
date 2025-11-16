package org.cmarket.cmarket.domain.profile.app.dto;

import java.util.List;

/**
 * 페이지네이션 결과 DTO
 * 
 * Spring Data Page를 직접 노출하지 않고 사용하는 전용 타입입니다.
 * 아키텍처 가이드에 따라 PageResult를 사용합니다.
 */
public record PageResult<T>(
    int page,
    int size,
    long total,
    List<T> content,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious,
    long totalElements,
    long numberOfElements
) {
    /**
     * Spring Data Page에서 PageResult로 변환
     * 
     * @param page Spring Data Page 객체
     * @return PageResult
     */
    public static <T> PageResult<T> fromPage(org.springframework.data.domain.Page<T> page) {
        return new PageResult<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getContent(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                page.getTotalElements(),
                page.getNumberOfElements()
        );
    }
    
    /**
     * PageResult의 content를 다른 타입으로 변환
     * 
     * @param mapper 변환 함수
     * @return 변환된 PageResult
     */
    public <R> PageResult<R> map(java.util.function.Function<T, R> mapper) {
        List<R> mappedContent = content.stream()
                .map(mapper)
                .toList();
        
        return new PageResult<>(
                page,
                size,
                total,
                mappedContent,
                totalPages,
                hasNext,
                hasPrevious,
                totalElements,
                numberOfElements
        );
    }
}

