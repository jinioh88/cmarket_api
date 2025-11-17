package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.ProductListItemDto;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 상품 목록 항목 응답 DTO
 * 
 * 상품 목록에서 사용하는 개별 상품 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProductListItemResponse {
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
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto ProductListItemDto
     * @return ProductListItemResponse
     */
    public static ProductListItemResponse fromDto(ProductListItemDto dto) {
        ProductListItemResponse response = new ProductListItemResponse();
        response.id = dto.getId();
        response.mainImageUrl = dto.getMainImageUrl();
        response.petDetailType = dto.getPetDetailType();
        response.productStatus = dto.getProductStatus();
        response.tradeStatus = dto.getTradeStatus();
        response.title = dto.getTitle();
        response.price = dto.getPrice();
        response.createdAt = dto.getCreatedAt();
        response.favoriteCount = dto.getFavoriteCount();
        response.isFavorite = dto.getIsFavorite();
        return response;
    }
}

