package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.PostDetailDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 응답 DTO
 *
 * 게시글 상세 정보를 반환하는 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class PostDetailResponse {
    private Long id;
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String title;
    private String content;
    private List<String> imageUrls;
    private Long viewCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentSummaryResponse> comments;

    /**
     * 앱 DTO를 웹 DTO로 변환
     *
     * @param dto PostDetailDto
     * @return PostDetailResponse
     */
    public static PostDetailResponse fromDto(PostDetailDto dto) {
        PostDetailResponse response = new PostDetailResponse();
        response.id = dto.getId();
        response.authorId = dto.getAuthorId();
        response.authorNickname = dto.getAuthorNickname();
        response.authorProfileImageUrl = dto.getAuthorProfileImageUrl();
        response.title = dto.getTitle();
        response.content = dto.getContent();
        response.imageUrls = dto.getImageUrls();
        response.viewCount = dto.getViewCount();
        response.commentCount = dto.getCommentCount();
        response.createdAt = dto.getCreatedAt();
        response.updatedAt = dto.getUpdatedAt();
        response.comments = dto.getComments().stream()
                .map(CommentSummaryResponse::fromDto)
                .toList();
        return response;
    }
}

