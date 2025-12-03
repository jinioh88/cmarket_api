package org.cmarket.cmarket.domain.community.repository;

import org.cmarket.cmarket.domain.community.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Post 엔티티 커스텀 레포지토리 인터페이스
 * 
 * QueryDSL을 사용한 복잡한 쿼리를 정의합니다.
 */
public interface PostRepositoryCustom {
    
    /**
     * 게시글 목록 조회 (정렬 지원)
     * 
     * @param sortBy 정렬 기준 ("latest", "oldest", "views", "comments")
     * @param sortOrder 정렬 방향 ("asc", "desc") - 기본값 "desc"
     * @param pageable 페이지네이션 정보
     * @return 게시글 목록 (페이지네이션)
     */
    Page<Post> findPosts(String sortBy, String sortOrder, Pageable pageable);
}

