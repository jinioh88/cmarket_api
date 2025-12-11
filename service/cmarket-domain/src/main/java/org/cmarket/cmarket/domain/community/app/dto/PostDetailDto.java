package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 DTO
 *
 * 게시글 상세 조회 결과를 담는 앱 DTO입니다.
 */
@Getter
@Builder
public class PostDetailDto {
    private Long id;
    private Long authorId;
    private String authorNickname;
    private String authorProfileImageUrl;
    private String title;
    private String content;
    private List<String> imageUrls;
    private BoardType boardType;
    private Long viewCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Post 엔티티를 PostDetailDto로 변환
     *
     * @param post 게시글 엔티티
     * @param authorNickname 작성자 닉네임
     * @param authorProfileImageUrl 작성자 프로필 이미지 URL
     * @return PostDetailDto
     */
    public static PostDetailDto fromEntity(
            Post post,
            String authorNickname,
            String authorProfileImageUrl
    ) {
        return PostDetailDto.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .authorNickname(authorNickname)
                .authorProfileImageUrl(authorProfileImageUrl)
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .boardType(post.getBoardType())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

