package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.CommentSummaryDto;

import java.time.LocalDateTime;

/**
 * 댓글 요약 응답 DTO
 *
 * 게시글 상세 조회에서 사용하는 댓글 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class CommentSummaryResponse {
    private Long id;
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String content;
    private LocalDateTime createdAt;
    private Integer depth;
    private Long parentId;

    /**
     * 앱 DTO를 웹 DTO로 변환
     *
     * @param dto CommentSummaryDto
     * @return CommentSummaryResponse
     */
    public static CommentSummaryResponse fromDto(CommentSummaryDto dto) {
        CommentSummaryResponse response = new CommentSummaryResponse();
        response.id = dto.getId();
        response.authorId = dto.getAuthorId();
        response.authorNickname = dto.getAuthorNickname();
        response.authorProfileImageUrl = dto.getAuthorProfileImageUrl();
        response.content = dto.getContent();
        response.createdAt = dto.getCreatedAt();
        response.depth = dto.getDepth();
        response.parentId = dto.getParentId();
        return response;
    }
}

