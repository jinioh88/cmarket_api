package org.cmarket.cmarket.domain.community.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 댓글 엔티티
 * 
 * 커뮤니티 게시글의 댓글 정보를 저장하는 도메인 모델입니다.
 * - 대댓글/대대댓글 지원 (최대 3단계 depth)
 * - 소프트 삭제 지원 (deletedAt)
 * - parentId로 부모 댓글 참조
 */
@Entity
@Table(
    name = "comments"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "post_id")
    private Long postId;  // 게시글 ID (Post 참조)
    
    @Column(nullable = false, name = "author_id")
    private Long authorId;  // 작성자 ID (User 참조)
    
    @Column(nullable = false, name = "author_nickname", length = 10)
    private String authorNickname;  // 작성자 닉네임 (작성 시점 스냅샷)
    
    @Column(name = "author_profile_image_url", length = 500)
    private String authorProfileImageUrl;  // 작성자 프로필 이미지 URL (작성 시점 스냅샷)
    
    @Column(name = "parent_id")
    private Long parentId;  // 부모 댓글 ID (대댓글/대대댓글용, nullable)
    
    @Column(nullable = false, length = 500)
    private String content;  // 내용 (2-500자)
    
    @Column(nullable = false)
    private Integer depth;  // 댓글 깊이 (1=댓글, 2=대댓글, 3=대대댓글)
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // 소프트 삭제용 (null이면 활성, 값이 있으면 삭제됨)
    
    @Builder
    public Comment(
            Long postId,
            Long authorId,
            String authorNickname,
            String authorProfileImageUrl,
            Long parentId,
            String content,
            Integer depth
    ) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorNickname = authorNickname;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.parentId = parentId;
        this.content = content;
        this.depth = depth;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 댓글 내용 수정
     */
    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 소프트 삭제 처리
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}

