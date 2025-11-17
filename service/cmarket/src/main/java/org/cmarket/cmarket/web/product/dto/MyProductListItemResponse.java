package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.MyProductListItemDto;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 내가 등록한 상품 목록 항목 응답 DTO
 * 
 * 내가 등록한 상품 목록에서 사용하는 개별 상품 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class MyProductListItemResponse {
    private Long id;
    private String mainImageUrl;
    private String title;
    private Long price;
    private Long viewCount;
    private TradeStatus tradeStatus;
    private LocalDateTime createdAt;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto MyProductListItemDto
     * @return MyProductListItemResponse
     */
    public static MyProductListItemResponse fromDto(MyProductListItemDto dto) {
        MyProductListItemResponse response = new MyProductListItemResponse();
        response.id = dto.getId();
        response.mainImageUrl = dto.getMainImageUrl();
        response.title = dto.getTitle();
        response.price = dto.getPrice();
        response.viewCount = dto.getViewCount();
        response.tradeStatus = dto.getTradeStatus();
        response.createdAt = dto.getCreatedAt();
        return response;
    }
}

