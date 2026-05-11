package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 관심 목록 항목 DTO
 *
 * 관심 목록에서 사용하는 개별 상품 정보 DTO입니다.
 */
@Getter
@Builder
public class FavoriteItemDto {
    private Long id;
    private String mainImageUrl;
    private String title;
    private Long price;
    private Long viewCount;
    private TradeStatus tradeStatus;
    private ProductType productType;
    private ProductStatus productStatus;
    private PetDetailType petDetailType;
    private Long favoriteCount;
    private Boolean isFavorite;
    private String addressSido;
    private String addressGugun;
    private LocalDateTime createdAt;
}

