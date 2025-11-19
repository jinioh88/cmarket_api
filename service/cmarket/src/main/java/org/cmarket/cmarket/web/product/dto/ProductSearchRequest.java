package org.cmarket.cmarket.web.product.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;

import java.util.List;

/**
 * 상품 통합 검색 요청 DTO
 * 
 * 검색, 필터링, 정렬 모든 조건을 포함하는 통합 요청 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProductSearchRequest {
    
    private String keyword;  // 검색어 (제목, 설명, 카테고리명 검색)
    
    private ProductType productType;  // 상품 타입 - 전체(null)/판매(SELL)/판매요청(REQUEST) (화면의 탭 기능)
    
    private PetType petType;  // 반려동물 대분류
    
    private PetDetailType petDetailType;  // 반려동물 상세 종류
    
    private List<Category> categories;  // 상품 카테고리 리스트 (여러 개 선택 가능)
    
    private List<ProductStatus> productStatuses;  // 상품 상태 리스트 (여러 개 선택 가능)
    
    @Min(0)
    private Long minPrice;  // 최소 가격
    
    @Min(0)
    private Long maxPrice;  // 최대 가격
    
    private String addressSido;  // 시/도
    
    private String addressGugun;  // 시/군/구
    
    private String sortBy;  // 정렬 기준 - "createdAt" | "price" | "favoriteCount" (기본값: "createdAt")
    
    private String sortOrder;  // 정렬 방향 - "asc" | "desc" (기본값: "desc")
    
    @Min(0)
    private Integer page;  // 페이지 번호 (기본값: 0)
    
    @Min(1)
    private Integer size;  // 페이지 크기 (기본값: 20)
}

