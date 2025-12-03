package org.cmarket.cmarket.domain.community.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 커뮤니티 게시글 엔티티
 * 
 * 커뮤니티 게시판의 게시글 정보를 저장하는 도메인 모델입니다.
 * - 소프트 삭제 지원 (deletedAt)
 * - 조회수와 댓글 개수 관리
 * - 이미지 최대 5장 지원
 */
@Entity
@Table(
    name = "posts",
    indexes = {
        @Index(name = "idx_author_id", columnList = "author_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "author_id")
    private Long authorId;  // 작성자 ID (User 참조)
    
    @Column(nullable = false, name = "author_nickname", length = 10)
    private String authorNickname;  // 작성자 닉네임 (작성 시점 스냅샷)
    
    @Column(name = "author_profile_image_url", length = 500)
    private String authorProfileImageUrl;  // 작성자 프로필 이미지 URL (작성 시점 스냅샷)
    
    @Column(nullable = false, length = 50)
    private String title;  // 제목 (2-50자)
    
    @Column(nullable = false, length = 1000)
    private String content;  // 내용 (2-1000자)
    
    @ElementCollection
    @CollectionTable(
        name = "post_images",
        joinColumns = @JoinColumn(name = "post_id")
    )
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();  // 이미지 URL 리스트 (최대 5장)
    
    @Column(nullable = false, name = "view_count")
    private Long viewCount = 0L;  // 조회수 (기본값 0)
    
    @Column(nullable = false, name = "comment_count")
    private Long commentCount = 0L;  // 댓글 개수 (기본값 0)
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // 소프트 삭제용 (null이면 활성, 값이 있으면 삭제됨)
    
    @Builder
    public Post(
            Long authorId,
            String authorNickname,
            String authorProfileImageUrl,
            String title,
            String content,
            List<String> imageUrls
    ) {
        this.authorId = authorId;
        this.authorNickname = authorNickname;
        this.authorProfileImageUrl = authorProfileImageUrl;
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.viewCount = 0L;
        this.commentCount = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 게시글 정보 수정
     */
    public void update(String title, String content, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 조회수 증가
     */
    public void increaseViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 댓글 개수 증가
     */
    public void increaseCommentCount() {
        this.commentCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 댓글 개수 감소
     */
    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
            this.updatedAt = LocalDateTime.now();
        }
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

