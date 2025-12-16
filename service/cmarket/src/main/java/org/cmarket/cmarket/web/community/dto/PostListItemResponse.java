package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.PostListItemDto;
import org.cmarket.cmarket.domain.community.model.BoardType;

import java.time.LocalDateTime;

/**
 * 게시글 목록 항목 응답 DTO
 * 
 * 게시글 목록에서 사용하는 개별 게시글 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class PostListItemResponse {
    private Long id;
    private String title;
    private String contentPreview;  // 내용 미리보기 (최대 100자)
    private String authorNickname;
    private BoardType boardType;
    private Long viewCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isModified;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto PostListItemDto
     * @return PostListItemResponse
     */
    public static PostListItemResponse fromDto(PostListItemDto dto) {
        PostListItemResponse response = new PostListItemResponse();
        response.id = dto.getId();
        response.title = dto.getTitle();
        
        // content를 100글자로 제한
        String content = dto.getContent();
        if (content != null && content.length() > 100) {
            response.contentPreview = content.substring(0, 100);
        } else {
            response.contentPreview = content;
        }
        
        response.authorNickname = dto.getAuthorNickname();
        response.boardType = dto.getBoardType();
        response.viewCount = dto.getViewCount();
        response.commentCount = dto.getCommentCount();
        response.createdAt = dto.getCreatedAt();
        response.updatedAt = dto.getUpdatedAt();
        response.isModified = dto.getIsModified();
        return response;
    }
}

