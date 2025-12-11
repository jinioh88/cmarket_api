package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.CommentListItemDto;

import java.time.LocalDateTime;

/**
 * 댓글 목록 항목 응답 DTO
 * 
 * 댓글 목록에서 사용하는 개별 댓글 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class CommentListItemResponse {
    private Long id;
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String content;
    private LocalDateTime createdAt;
    private Integer depth;
    private Long parentId;
    private Boolean hasChildren;
    private Integer childrenCount;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto CommentListItemDto
     * @return CommentListItemResponse
     */
    public static CommentListItemResponse fromDto(CommentListItemDto dto) {
        CommentListItemResponse response = new CommentListItemResponse();
        response.id = dto.getId();
        response.authorId = dto.getAuthorId();
        response.authorNickname = dto.getAuthorNickname();
        response.authorProfileImageUrl = dto.getAuthorProfileImageUrl();
        response.content = dto.getContent();
        response.createdAt = dto.getCreatedAt();
        response.depth = dto.getDepth();
        response.parentId = dto.getParentId();
        response.hasChildren = dto.getHasChildren();
        response.childrenCount = dto.getChildrenCount();
        return response;
    }
}

