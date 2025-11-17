package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.FavoriteListDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

import java.util.List;

/**
 * 관심 목록 조회 응답 DTO
 * 
 * 관심 목록 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class FavoriteListResponse {
    private int page;
    private int size;
    private long total;
    private List<FavoriteItemResponse> content;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements;
    private long numberOfElements;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto FavoriteListDto
     * @return FavoriteListResponse
     */
    public static FavoriteListResponse fromDto(FavoriteListDto dto) {
        FavoriteListResponse response = new FavoriteListResponse();
        PageResult<org.cmarket.cmarket.domain.product.app.dto.FavoriteItemDto> pageResult = dto.favorites();
        
        response.page = pageResult.page();
        response.size = pageResult.size();
        response.total = pageResult.total();
        response.content = pageResult.content().stream()
                .map(FavoriteItemResponse::fromDto)
                .toList();
        response.totalPages = pageResult.totalPages();
        response.hasNext = pageResult.hasNext();
        response.hasPrevious = pageResult.hasPrevious();
        response.totalElements = pageResult.totalElements();
        response.numberOfElements = pageResult.numberOfElements();
        
        return response;
    }
}

