package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 상세 정보 DTO
 * 
 * 상품 상세 조회 결과를 담는 앱 DTO입니다.
 */
@Getter
@Builder
public class ProductDetailDto {
    // 상품 정보
    private Long id;
    private ProductType productType;
    private TradeStatus tradeStatus;
    private PetType petType;
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
    private Boolean isDeliveryAvailable;
    private String preferredMeetingPlace;
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorite;
    
    // 판매자 정보
    private SellerInfoDto sellerInfo;
    
    // 판매자의 다른 상품 목록
    private List<ProductListItemDto> sellerOtherProducts;
}

