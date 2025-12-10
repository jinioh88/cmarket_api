package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 정보 DTO
 * 
 * 앱 서비스에서 사용하는 게시글 정보 DTO입니다.
 */
@Getter
@Builder
public class PostDto {
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
     * Post 엔티티를 PostDto로 변환
     * 
     * @param post Post 엔티티
     * @return PostDto
     */
    public static PostDto fromEntity(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
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

