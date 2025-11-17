package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.ProductRequestDetailDto;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 판매 요청 상세 조회 응답 DTO
 * 
 * 판매 요청 상세 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProductRequestDetailResponse {
    private Long id;
    private ProductType productType;
    private TradeStatus tradeStatus;
    private PetDetailType petDetailType;
    private Category category;
    private String title;
    private String description;
    private Long desiredPrice;  // 희망 가격
    private String mainImageUrl;
    private List<String> subImageUrls;
    private String addressSido;
    private String addressGugun;
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long favoriteCount;
    private SellerInfoResponse sellerInfo;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto ProductRequestDetailDto
     * @return ProductRequestDetailResponse
     */
    public static ProductRequestDetailResponse fromDto(ProductRequestDetailDto dto) {
        ProductRequestDetailResponse response = new ProductRequestDetailResponse();
        response.id = dto.getId();
        response.productType = dto.getProductType();
        response.tradeStatus = dto.getTradeStatus();
        response.petDetailType = dto.getPetDetailType();
        response.category = dto.getCategory();
        response.title = dto.getTitle();
        response.description = dto.getDescription();
        response.desiredPrice = dto.getDesiredPrice();
        response.mainImageUrl = dto.getMainImageUrl();
        response.subImageUrls = dto.getSubImageUrls();
        response.addressSido = dto.getAddressSido();
        response.addressGugun = dto.getAddressGugun();
        response.createdAt = dto.getCreatedAt();
        response.viewCount = dto.getViewCount();
        response.favoriteCount = dto.getFavoriteCount();
        response.sellerInfo = SellerInfoResponse.fromDto(dto.getSellerInfo());
        return response;
    }
}

