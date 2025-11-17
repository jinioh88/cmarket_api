package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 내가 등록한 상품 목록 항목 DTO
 * 
 * 내가 등록한 상품 목록에서 사용하는 개별 상품 정보 DTO입니다.
 */
@Getter
@Builder
public class MyProductListItemDto {
    private Long id;
    private String mainImageUrl;
    private String title;
    private Long price;
    private Long viewCount;
    private TradeStatus tradeStatus;
    private LocalDateTime createdAt;
    
    /**
     * Product 엔티티를 MyProductListItemDto로 변환
     * 
     * @param product Product 엔티티
     * @return MyProductListItemDto
     */
    public static MyProductListItemDto fromEntity(Product product) {
        return MyProductListItemDto.builder()
                .id(product.getId())
                .mainImageUrl(product.getMainImageUrl())
                .title(product.getTitle())
                .price(product.getPrice())
                .viewCount(product.getViewCount())
                .tradeStatus(product.getTradeStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }
}

