package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.Comment;

import java.time.LocalDateTime;

/**
 * 댓글 요약 DTO
 *
 * 게시글 상세 조회에서 사용하는 댓글 정보 DTO입니다.
 */
@Getter
@Builder
public class CommentSummaryDto {
    private Long id;
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String content;
    private LocalDateTime createdAt;
    private Integer depth;
    private Long parentId;

    /**
     * Comment 엔티티를 CommentSummaryDto로 변환
     *
     * @param comment 댓글 엔티티
     * @param authorNickname 작성자 닉네임
     * @param authorProfileImageUrl 작성자 프로필 이미지 URL
     * @return CommentSummaryDto
     */
    public static CommentSummaryDto fromEntity(
            Comment comment,
            String authorNickname,
            String authorProfileImageUrl
    ) {
        return CommentSummaryDto.builder()
                .id(comment.getId())
                .authorId(comment.getAuthorId())
                .authorNickname(authorNickname)
                .authorProfileImageUrl(authorProfileImageUrl)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .depth(comment.getDepth())
                .parentId(comment.getParentId())
                .build();
    }
}

