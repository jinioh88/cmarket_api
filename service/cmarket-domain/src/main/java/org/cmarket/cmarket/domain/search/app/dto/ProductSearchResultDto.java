package org.cmarket.cmarket.domain.search.app.dto;

import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

/**
 * 상품 통합 검색 결과 DTO
 * 
 * 검색 결과를 담는 앱 DTO입니다.
 */
public record ProductSearchResultDto(
    PageResult<ProductSearchItemDto> products
) {
}

