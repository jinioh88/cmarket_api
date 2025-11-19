package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.search.app.dto.ProductSearchItemDto;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 상품 검색 결과 항목 응답 DTO
 * 
 * 검색 결과에서 사용하는 개별 상품 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProductSearchItemResponse {
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
     * @param dto ProductSearchItemDto
     * @return ProductSearchItemResponse
     */
    public static ProductSearchItemResponse fromDto(ProductSearchItemDto dto) {
        ProductSearchItemResponse response = new ProductSearchItemResponse();
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

