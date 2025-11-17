package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.ProductRequestListDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

/**
 * 판매 요청 목록 조회 응답 DTO
 * 
 * 판매 요청 목록 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class ProductRequestListResponse {
    private int page;
    private int size;
    private long total;
    private List<ProductRequestListItemResponse> content;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements;
    private long numberOfElements;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto ProductRequestListDto
     * @return ProductRequestListResponse
     */
    public static ProductRequestListResponse fromDto(ProductRequestListDto dto) {
        ProductRequestListResponse response = new ProductRequestListResponse();
        PageResult<org.cmarket.cmarket.domain.product.app.dto.ProductRequestListItemDto> pageResult = dto.products();
        
        response.page = pageResult.page();
        response.size = pageResult.size();
        response.total = pageResult.total();
        response.content = pageResult.content().stream()
                .map(ProductRequestListItemResponse::fromDto)
                .toList();
        response.totalPages = pageResult.totalPages();
        response.hasNext = pageResult.hasNext();
        response.hasPrevious = pageResult.hasPrevious();
        response.totalElements = pageResult.totalElements();
        response.numberOfElements = pageResult.numberOfElements();
        
        return response;
    }
}

