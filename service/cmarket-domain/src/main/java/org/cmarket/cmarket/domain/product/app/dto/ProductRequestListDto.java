package org.cmarket.cmarket.domain.product.app.dto;

import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

/**
 * 판매 요청 목록 조회 결과 DTO
 * 
 * 판매 요청 목록 조회 결과를 담는 앱 DTO입니다.
 */
public record ProductRequestListDto(
    PageResult<ProductRequestListItemDto> products
) {
}

