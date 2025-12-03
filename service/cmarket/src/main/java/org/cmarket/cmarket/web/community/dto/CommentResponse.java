package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.CommentDto;

import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO
 * 
 * 댓글 정보를 반환하는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private Long parentId;
    private String content;
    private Integer depth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param commentDto CommentDto
     * @return CommentResponse
     */
    public static CommentResponse fromDto(CommentDto commentDto) {
        CommentResponse response = new CommentResponse();
        response.id = commentDto.getId();
        response.postId = commentDto.getPostId();
        response.authorId = commentDto.getAuthorId();
        response.authorNickname = commentDto.getAuthorNickname();
        response.authorProfileImageUrl = commentDto.getAuthorProfileImageUrl();
        response.parentId = commentDto.getParentId();
        response.content = commentDto.getContent();
        response.depth = commentDto.getDepth();
        response.createdAt = commentDto.getCreatedAt();
        response.updatedAt = commentDto.getUpdatedAt();
        return response;
    }
}

