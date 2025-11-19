package org.cmarket.cmarket.web.product.controller;

import org.cmarket.cmarket.domain.search.app.dto.ProductSearchCommand;
import org.cmarket.cmarket.domain.search.app.service.SearchService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.product.dto.ProductSearchRequest;
import org.cmarket.cmarket.web.product.dto.ProductSearchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 관련 컨트롤러
 * 
 * 상품 통합 검색 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/products")
public class SearchController {
    
    private final SearchService searchService;
    
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * 상품 통합 검색 (검색 + 필터링 + 정렬)
     * 
     * GET /api/products/search
     * 
     * 상품을 검색하고 필터링 및 정렬할 수 있습니다.
     * - 키워드 검색: 제목, 설명, 카테고리명 검색
     * - 필터링: 상품 타입, 반려동물 종류, 카테고리, 가격대, 지역, 상품 상태
     * - 정렬: 최신순, 가격순, 찜 많은 순
     * - 로그인한 사용자는 찜 여부 표시
     * 
     * @param request 검색 요청 (모든 파라미터는 선택적)
     * @return 검색 결과 (페이지네이션 포함)
     */
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<ProductSearchResponse>> searchProducts(
            @ModelAttribute ProductSearchRequest request
    ) {
        // 현재 로그인한 사용자 이메일 추출 (선택적, 비로그인 시 null)
        String email = null;
        if (SecurityUtils.isAuthenticated()) {
            email = SecurityUtils.getCurrentUserEmail();
        }
        
        // 웹 DTO → 앱 DTO 변환
        ProductSearchCommand command = toCommand(request);
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.search.app.dto.ProductSearchResultDto resultDto = 
                searchService.searchProducts(command, email);
        
        // 앱 DTO → 웹 DTO 변환
        ProductSearchResponse response = ProductSearchResponse.fromDto(resultDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 웹 DTO를 앱 DTO로 변환
     */
    private ProductSearchCommand toCommand(ProductSearchRequest request) {
        return ProductSearchCommand.builder()
                .keyword(request.getKeyword())
                .productType(request.getProductType())
                .petType(request.getPetType())
                .petDetailType(request.getPetDetailType())
                .categories(request.getCategories())
                .productStatuses(request.getProductStatuses())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .addressSido(request.getAddressSido())
                .addressGugun(request.getAddressGugun())
                .sortBy(request.getSortBy())
                .sortOrder(request.getSortOrder())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
}

