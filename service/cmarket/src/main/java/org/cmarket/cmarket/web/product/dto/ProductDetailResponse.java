package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.ProductDetailDto;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 상세 응답 DTO
 * 
 * 상품 상세 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProductDetailResponse {
    // 상품 정보
    private Long id;
    private ProductType productType;
    private TradeStatus tradeStatus;
    private PetDetailType petDetailType;
    private Category category;
    private ProductStatus productStatus;
    private String title;
    private String description;
    private Long price;
    private String mainImageUrl;
    private List<String> subImageUrls;
    private String addressSido;
    private String addressGugun;
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorite;
    
    // 판매자 정보
    private SellerInfoResponse sellerInfo;
    
    // 판매자의 다른 상품 목록
    private List<ProductListItemResponse> sellerOtherProducts;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto ProductDetailDto
     * @return ProductDetailResponse
     */
    public static ProductDetailResponse fromDto(ProductDetailDto dto) {
        ProductDetailResponse response = new ProductDetailResponse();
        response.id = dto.getId();
        response.productType = dto.getProductType();
        response.tradeStatus = dto.getTradeStatus();
        response.petDetailType = dto.getPetDetailType();
        response.category = dto.getCategory();
        response.productStatus = dto.getProductStatus();
        response.title = dto.getTitle();
        response.description = dto.getDescription();
        response.price = dto.getPrice();
        response.mainImageUrl = dto.getMainImageUrl();
        response.subImageUrls = dto.getSubImageUrls();
        response.addressSido = dto.getAddressSido();
        response.addressGugun = dto.getAddressGugun();
        response.createdAt = dto.getCreatedAt();
        response.viewCount = dto.getViewCount();
        response.favoriteCount = dto.getFavoriteCount();
        response.isFavorite = dto.getIsFavorite();
        response.sellerInfo = SellerInfoResponse.fromDto(dto.getSellerInfo());
        response.sellerOtherProducts = dto.getSellerOtherProducts().stream()
                .map(ProductListItemResponse::fromDto)
                .toList();
        return response;
    }
}

