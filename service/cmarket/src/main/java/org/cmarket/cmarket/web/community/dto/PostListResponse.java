package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.PostListDto;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventInfoDto;

import java.util.List;
import java.util.Map;

/**
 * 게시글 목록 응답 DTO
 * 
 * 게시글 목록 조회 결과를 담는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class PostListResponse {
    private int page;
    private int size;
    private long total;
    private List<PostListItemResponse> content;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalElements;
    private long numberOfElements;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto PostListDto
     * @return PostListResponse
     */
    public static PostListResponse fromDto(PostListDto dto) {
        PostListResponse response = new PostListResponse();
        PageResult<org.cmarket.cmarket.domain.community.app.dto.PostListItemDto> pageResult = dto.posts();
        
        response.page = pageResult.page();
        response.size = pageResult.size();
        response.total = pageResult.total();
        response.content = pageResult.content().stream()
                .map(PostListItemResponse::fromDto)
                .toList();
        response.totalPages = pageResult.totalPages();
        response.hasNext = pageResult.hasNext();
        response.hasPrevious = pageResult.hasPrevious();
        response.totalElements = pageResult.totalElements();
        response.numberOfElements = pageResult.numberOfElements();
        
        return response;
    }

    /**
     * 이 페이지에 있는 글 번호 (행사 칸을 한 번에 찾으려고 쓴다)
     */
    public List<Long> getPostIds() {
        return content.stream().map(PostListItemResponse::getId).toList();
    }

    /**
     * 행사 글에 행사 칸을 붙인다. Map에 없는 글은 그대로 null이다.
     */
    public void attachEvents(Map<Long, EventInfoDto> events) {
        content.forEach(item -> item.attachEvent(EventInfoResponse.from(events.get(item.getId()))));
    }
}
