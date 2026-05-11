package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.FavoriteItemDto;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 관심 목록 항목 응답 DTO
 *
 * 관심 목록에서 사용하는 개별 상품 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class FavoriteItemResponse {
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

    /**
     * 앱 DTO를 웹 DTO로 변환
     *
     * @param dto FavoriteItemDto
     * @return FavoriteItemResponse
     */
    public static FavoriteItemResponse fromDto(FavoriteItemDto dto) {
        FavoriteItemResponse response = new FavoriteItemResponse();
        response.id = dto.getId();
        response.mainImageUrl = dto.getMainImageUrl();
        response.title = dto.getTitle();
        response.price = dto.getPrice();
        response.viewCount = dto.getViewCount();
        response.tradeStatus = dto.getTradeStatus();
        response.productType = dto.getProductType();
        response.productStatus = dto.getProductStatus();
        response.petDetailType = dto.getPetDetailType();
        response.favoriteCount = dto.getFavoriteCount();
        response.isFavorite = dto.getIsFavorite();
        response.addressSido = dto.getAddressSido();
        response.addressGugun = dto.getAddressGugun();
        response.createdAt = dto.getCreatedAt();
        return response;
    }
}

