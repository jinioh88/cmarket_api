package org.cmarket.cmarket.domain.product.repository;

import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Product 엔티티 커스텀 레포지토리 인터페이스
 * 
 * QueryDSL을 사용한 복잡한 검색 쿼리를 정의합니다.
 */
public interface ProductRepositoryCustom {
    
    /**
     * 상품 통합 검색 (검색 + 필터링 + 정렬)
     * 
     * @param keyword 검색어 (제목, 설명, 카테고리명 검색) - null 가능
     * @param keywords 다중 키워드 리스트 (AND 조건) - null 가능
     * @param productType 상품 타입 (전체/판매/판매요청) - null 가능
     * @param petType 반려동물 대분류 - null 가능
     * @param petDetailType 반려동물 상세 종류 - null 가능
     * @param categories 상품 카테고리 리스트 (여러 개 선택 가능) - null 또는 빈 리스트 가능
     * @param productStatuses 상품 상태 리스트 (여러 개 선택 가능) - null 또는 빈 리스트 가능
     * @param minPrice 최소 가격 - null 가능
     * @param maxPrice 최대 가격 - null 가능
     * @param addressSido 시/도 - null 가능
     * @param addressGugun 시/군/구 - null 가능
     * @param sortBy 정렬 기준 ("createdAt", "price", "favoriteCount")
     * @param sortOrder 정렬 방향 ("asc", "desc")
     * @param pageable 페이지네이션 정보
     * @return 검색된 상품 목록 (페이지네이션)
     */
    Page<Product> searchProducts(
            String keyword,
            List<String> keywords,
            ProductType productType,
            PetType petType,
            PetDetailType petDetailType,
            List<Category> categories,
            List<ProductStatus> productStatuses,
            Long minPrice,
            Long maxPrice,
            String addressSido,
            String addressGugun,
            String sortBy,
            String sortOrder,
            Pageable pageable
    );
}

