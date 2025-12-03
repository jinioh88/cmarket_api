package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.Comment;

import java.time.LocalDateTime;

/**
 * 댓글 정보 DTO
 * 
 * 앱 서비스에서 사용하는 댓글 정보 DTO입니다.
 */
@Getter
@Builder
public class CommentDto {
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
     * Comment 엔티티를 CommentDto로 변환
     * 
     * @param comment Comment 엔티티
     * @return CommentDto
     */
    public static CommentDto fromEntity(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .authorNickname(comment.getAuthorNickname())
                .authorProfileImageUrl(comment.getAuthorProfileImageUrl())
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .depth(comment.getDepth())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

