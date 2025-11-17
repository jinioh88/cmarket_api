package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.FavoriteItemDto;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

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
        return response;
    }
}

