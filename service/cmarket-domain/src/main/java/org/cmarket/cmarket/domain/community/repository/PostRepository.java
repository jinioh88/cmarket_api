package org.cmarket.cmarket.domain.community.repository;

import org.cmarket.cmarket.domain.community.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Post 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 작성자별 게시글 목록 조회
 * - 게시글 존재 확인
 * - 소프트 삭제된 게시글 제외 조회
 */
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    
    /**
     * 작성자별 게시글 목록 조회 (페이지네이션, 최신순 정렬)
     * 
     * @param authorId 작성자 ID
     * @param pageable 페이지네이션 정보
     * @return 게시글 목록 (최신순 정렬)
     */
    Page<Post> findByAuthorIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long authorId, Pageable pageable);
    
    /**
     * 게시글 조회 (소프트 삭제된 게시글 제외)
     * 
     * @param id 게시글 ID
     * @return 게시글 (없으면 Optional.empty())
     */
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);
    
    /**
     * 게시글 존재 확인 (소프트 삭제된 게시글 제외)
     * 
     * @param id 게시글 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByIdAndDeletedAtIsNull(Long id);
}

