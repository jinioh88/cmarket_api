package org.cmarket.cmarket.domain.search.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 상품 검색 결과 항목 DTO
 * 
 * 검색 결과에서 사용하는 개별 상품 정보 DTO입니다.
 */
@Getter
@Builder
public class ProductSearchItemDto {
    private Long id;
    private String mainImageUrl;
    private PetDetailType petDetailType;
    private ProductStatus productStatus;
    private ProductType productType;
    private TradeStatus tradeStatus;
    private String title;
    private Long price;
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorite;
    
    /**
     * Product 엔티티를 ProductSearchItemDto로 변환
     * 
     * @param product Product 엔티티
     * @param isFavorite 찜 여부
     * @return ProductSearchItemDto
     */
    public static ProductSearchItemDto fromEntity(Product product, Boolean isFavorite) {
        return ProductSearchItemDto.builder()
                .id(product.getId())
                .mainImageUrl(product.getMainImageUrl())
                .petDetailType(product.getPetDetailType())
                .productStatus(product.getProductStatus())
                .productType(product.getProductType())
                .tradeStatus(product.getTradeStatus())
                .title(product.getTitle())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .isFavorite(isFavorite != null ? isFavorite : false)
                .build();
    }
}

