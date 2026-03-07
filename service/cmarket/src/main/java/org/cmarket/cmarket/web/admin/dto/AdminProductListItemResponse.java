package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.AdminProductListItemDto;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;

/**
 * 어드민 상품 목록 항목 응답 DTO
 */
@Getter
@Builder
public class AdminProductListItemResponse {
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

    public static AdminProductListItemResponse fromDto(AdminProductListItemDto dto) {
        return AdminProductListItemResponse.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .price(dto.getPrice())
                .productType(dto.getProductType())
                .category(dto.getCategory())
                .petDetailType(dto.getPetDetailType())
                .productStatus(dto.getProductStatus())
                .tradeStatus(dto.getTradeStatus())
                .sellerNickname(dto.getSellerNickname())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
