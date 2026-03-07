package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 어드민 상품 목록 항목 DTO
 */
@Getter
@Builder
public class AdminProductListItemDto {
    private Long id;
    private String title;
    private Long price;
    private ProductType productType;
    private Category category;
    private PetDetailType petDetailType;
    private ProductStatus productStatus;
    private TradeStatus tradeStatus;
    private String sellerNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
