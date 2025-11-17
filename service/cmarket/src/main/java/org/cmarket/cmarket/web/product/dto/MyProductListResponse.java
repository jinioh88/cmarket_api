package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.MyProductListDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

/**
 * 내가 등록한 상품 목록 조회 응답 DTO
 * 
 * 내가 등록한 상품 목록 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class MyProductListResponse {
    private int page;
    private int size;
    private long total;
    private List<MyProductListItemResponse> content;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements;
    private long numberOfElements;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto MyProductListDto
     * @return MyProductListResponse
     */
    public static MyProductListResponse fromDto(MyProductListDto dto) {
        MyProductListResponse response = new MyProductListResponse();
        PageResult<org.cmarket.cmarket.domain.product.app.dto.MyProductListItemDto> pageResult = dto.products();
        
        response.page = pageResult.page();
        response.size = pageResult.size();
        response.total = pageResult.total();
        response.content = pageResult.content().stream()
                .map(MyProductListItemResponse::fromDto)
                .toList();
        response.totalPages = pageResult.totalPages();
        response.hasNext = pageResult.hasNext();
        response.hasPrevious = pageResult.hasPrevious();
        response.totalElements = pageResult.totalElements();
        response.numberOfElements = pageResult.numberOfElements();
        
        return response;
    }
}

