package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 판매자 정보 DTO
 * 
 * 상품 상세 조회에서 사용하는 판매자 정보 DTO입니다.
 */
@Getter
@Builder
public class SellerInfoDto {
    private Long sellerId;
    private String sellerNickname;
    private String sellerProfileImageUrl;
}

