package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.Comment;

import java.time.LocalDateTime;

/**
 * 댓글 목록 항목 DTO
 * 
 * 댓글 목록에서 사용하는 개별 댓글 정보 DTO입니다.
 */
@Getter
@Builder
public class CommentListItemDto {
    private Long id;
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String content;
    private LocalDateTime createdAt;
    private Integer depth;
    private Long parentId;
    private Boolean hasChildren;  // 하위 댓글 존재 여부
    
    /**
     * Comment 엔티티를 CommentListItemDto로 변환
     * 
     * @param comment Comment 엔티티
     * @param hasChildren 하위 댓글 존재 여부
     * @return CommentListItemDto
     */
    public static CommentListItemDto fromEntity(Comment comment, Boolean hasChildren) {
        return CommentListItemDto.builder()
                .id(comment.getId())
                .authorId(comment.getAuthorId())
                .authorNickname(comment.getAuthorNickname() != null ? comment.getAuthorNickname() : "탈퇴한 사용자")
                .authorProfileImageUrl(comment.getAuthorProfileImageUrl())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .depth(comment.getDepth())
                .parentId(comment.getParentId())
                .hasChildren(hasChildren != null ? hasChildren : false)
                .build();
    }
}

