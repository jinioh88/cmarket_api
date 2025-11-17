package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;

import java.util.List;

/**
 * 판매 요청 수정 명령 DTO
 * 
 * 판매 요청 수정 시 필요한 모든 정보를 담는 앱 DTO입니다.
 */
@Builder
public record ProductRequestUpdateCommand(
    PetType petType,
    PetDetailType petDetailType,
    Category category,
    String title,
    String description,
    Long desiredPrice,
    String mainImageUrl,
    List<String> subImageUrls,
    String addressSido,
    String addressGugun
) {
}

