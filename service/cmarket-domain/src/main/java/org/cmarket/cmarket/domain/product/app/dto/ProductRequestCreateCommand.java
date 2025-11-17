package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;

import java.util.List;

/**
 * 판매 요청 등록 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class ProductRequestCreateCommand {
    // sellerId는 앱 서비스에서 email로 조회하여 설정
    private PetType petType;
    private PetDetailType petDetailType;
    private Category category;
    private String title;
    private String description;
    private Long desiredPrice;  // 희망 가격
    private String mainImageUrl;
    private List<String> subImageUrls;
    private String addressSido;
    private String addressGugun;
}

