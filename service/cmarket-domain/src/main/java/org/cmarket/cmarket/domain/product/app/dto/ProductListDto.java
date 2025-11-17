package org.cmarket.cmarket.domain.product.app.dto;

import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

/**
 * 상품 목록 DTO
 * 
 * 상품 목록 조회 결과를 담는 앱 DTO입니다.
 */
public record ProductListDto(
    PageResult<ProductListItemDto> products
) {
}

