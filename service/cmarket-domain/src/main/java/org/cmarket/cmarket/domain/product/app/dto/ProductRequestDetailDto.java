package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 판매 요청 상세 정보 DTO
 * 
 * 판매 요청 상세 조회 결과를 담는 앱 DTO입니다.
 */
@Getter
@Builder
public class ProductRequestDetailDto {
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
    private SellerInfoDto sellerInfo;
}

