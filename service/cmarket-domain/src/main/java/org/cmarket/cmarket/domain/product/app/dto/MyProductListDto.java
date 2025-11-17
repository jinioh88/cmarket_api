package org.cmarket.cmarket.domain.product.app.dto;

import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

/**
 * 내가 등록한 상품 목록 조회 결과 DTO
 * 
 * 내가 등록한 상품 목록 조회 결과를 담는 앱 DTO입니다.
 */
public record MyProductListDto(
    PageResult<MyProductListItemDto> products
) {
}

