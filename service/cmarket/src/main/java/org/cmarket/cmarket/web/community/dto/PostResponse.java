package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.PostDto;
import org.cmarket.cmarket.domain.community.model.BoardType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 응답 DTO
 * 
 * 게시글 정보를 반환하는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private Long authorId;
    private String title;
    private String content;
    private List<String> imageUrls;
    private BoardType boardType;
    private Long viewCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param postDto PostDto
     * @return PostResponse
     */
    public static PostResponse fromDto(PostDto postDto) {
        PostResponse response = new PostResponse();
        response.id = postDto.getId();
        response.authorId = postDto.getAuthorId();
        response.title = postDto.getTitle();
        response.content = postDto.getContent();
        response.imageUrls = postDto.getImageUrls();
        response.boardType = postDto.getBoardType();
        response.viewCount = postDto.getViewCount();
        response.commentCount = postDto.getCommentCount();
        response.createdAt = postDto.getCreatedAt();
        response.updatedAt = postDto.getUpdatedAt();
        return response;
    }
}

