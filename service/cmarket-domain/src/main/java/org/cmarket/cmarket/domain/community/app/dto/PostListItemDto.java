package org.cmarket.cmarket.domain.community.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.Post;

import java.time.LocalDateTime;

/**
 * 게시글 목록 항목 DTO
 * 
 * 게시글 목록에서 사용하는 개별 게시글 정보 DTO입니다.
 */
@Getter
@Builder
public class PostListItemDto {
    private Long id;
    private String title;
    private String authorNickname;
    private Long viewCount;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isModified;  // updatedAt != createdAt
    
    /**
     * Post 엔티티를 PostListItemDto로 변환
     * 
     * @param post Post 엔티티
     * @param authorNickname 작성자 닉네임
     * @return PostListItemDto
     */
    public static PostListItemDto fromEntity(Post post, String authorNickname) {
        boolean isModified = !post.getCreatedAt().equals(post.getUpdatedAt());
        
        return PostListItemDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorNickname(authorNickname)
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isModified(isModified)
                .build();
    }
}

