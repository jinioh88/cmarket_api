package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;

import java.util.List;

/**
 * 상품 수정 명령 DTO
 * 
 * 상품 수정 시 필요한 모든 정보를 담는 앱 DTO입니다.
 */
@Builder
public record ProductUpdateCommand(
    PetType petType,
    PetDetailType petDetailType,
    Category category,
    String title,
    String description,
    Long price,
    ProductStatus productStatus,
    String mainImageUrl,
    List<String> subImageUrls,
    String addressSido,
    String addressGugun
) {
}

