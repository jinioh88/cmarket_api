package org.cmarket.cmarket.domain.search.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import java.util.List;

/**
 * 상품 통합 검색 커맨드 DTO
 * 
 * 앱 서비스에서 사용하는 검색 조건 DTO입니다.
 */
@Getter
@Builder
public class ProductSearchCommand {
    private String keyword;
    private List<String> keywords;  // 다중 키워드 리스트 (AND 조건)
    private ProductType productType;
    private PetType petType;
    private PetDetailType petDetailType;
    private List<Category> categories;
    private List<ProductStatus> productStatuses;
    private Long minPrice;
    private Long maxPrice;
    private String addressSido;
    private String addressGugun;
    private String sortBy;
    private String sortOrder;
    private Integer page;  // 페이지 번호 (기본값: 0)
    private Integer size;  // 페이지 크기 (기본값: 20)
}

