package org.cmarket.cmarket.domain.search.app.service;

import org.cmarket.cmarket.domain.search.app.dto.ProductSearchCommand;
import org.cmarket.cmarket.domain.search.app.dto.ProductSearchResultDto;

/**
 * 검색 서비스 인터페이스
 * 
 * 상품 통합 검색 기능을 제공합니다.
 */
public interface SearchService {
    
    /**
     * 상품 통합 검색 (검색 + 필터링 + 정렬)
     * 
     * @param command 검색 조건
     * @param email 현재 로그인한 사용자 이메일 (비로그인 시 null)
     * @return 검색 결과
     */
    ProductSearchResultDto searchProducts(ProductSearchCommand command, String email);
}

