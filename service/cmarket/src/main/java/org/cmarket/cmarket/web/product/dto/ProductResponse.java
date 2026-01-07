package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.ProductDto;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 응답 DTO
 * 
 * 상품 정보를 반환하는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private Long sellerId;
    private ProductType productType;
    private PetType petType;
    private PetDetailType petDetailType;
    private Category category;
    private String title;
    private String description;
    private Long price;
    private ProductStatus productStatus;
    private TradeStatus tradeStatus;
    private String mainImageUrl;
    private List<String> subImageUrls;
    private String addressSido;
    private String addressGugun;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param productDto ProductDto
     * @return ProductResponse
     */
    public static ProductResponse fromDto(ProductDto productDto) {
        ProductResponse response = new ProductResponse();
        response.id = productDto.getId();
        response.sellerId = productDto.getSellerId();
        response.productType = productDto.getProductType();
        response.petType = productDto.getPetType();
        response.petDetailType = productDto.getPetDetailType();
        response.category = productDto.getCategory();
        response.title = productDto.getTitle();
        response.description = productDto.getDescription();
        response.price = productDto.getPrice();
        response.productStatus = productDto.getProductStatus();
        response.tradeStatus = productDto.getTradeStatus();
        response.mainImageUrl = productDto.getMainImageUrl();
        response.subImageUrls = productDto.getSubImageUrls();
        response.addressSido = productDto.getAddressSido();
        response.addressGugun = productDto.getAddressGugun();
        response.viewCount = productDto.getViewCount();
        response.favoriteCount = productDto.getFavoriteCount();
        response.isFavorite = productDto.getIsFavorite();
        response.createdAt = productDto.getCreatedAt();
        response.updatedAt = productDto.getUpdatedAt();
        return response;
    }
}


