package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 판매 요청 목록 항목 DTO
 * 
 * 판매 요청 목록에서 사용하는 개별 상품 정보 DTO입니다.
 */
@Getter
@Builder
public class ProductRequestListItemDto {
    private Long id;
    private String mainImageUrl;
    private PetDetailType petDetailType;
    private ProductStatus productStatus;
    private TradeStatus tradeStatus;
    private String title;
    private Long price;
    private LocalDateTime createdAt;
    private Long favoriteCount;
    private Boolean isFavorite;
    
    /**
     * Product 엔티티를 ProductRequestListItemDto로 변환
     * 
     * @param product Product 엔티티
     * @param isFavorite 찜 여부
     * @return ProductRequestListItemDto
     */
    public static ProductRequestListItemDto fromEntity(Product product, Boolean isFavorite) {
        return ProductRequestListItemDto.builder()
                .id(product.getId())
                .mainImageUrl(product.getMainImageUrl())
                .petDetailType(product.getPetDetailType())
                .productStatus(product.getProductStatus())
                .tradeStatus(product.getTradeStatus())
                .title(product.getTitle())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .favoriteCount(product.getFavoriteCount())
                .isFavorite(isFavorite != null ? isFavorite : false)
                .build();
    }
}

