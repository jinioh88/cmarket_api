package org.cmarket.cmarket.domain.community.repository;

import org.cmarket.cmarket.domain.community.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Comment 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 게시글별 댓글 목록 조회
 * - 부모 댓글별 하위 댓글 목록 조회
 * - 게시글별 댓글 개수 조회
 * - 댓글 존재 확인
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 게시글별 댓글 목록 조회 (부모 댓글만, 최신순 정렬)
     * 
     * @param postId 게시글 ID
     * @return 부모 댓글 목록 (최신순 정렬)
     */
    List<Comment> findByPostIdAndParentIdIsNullAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId);
    
    /**
     * 부모 댓글별 하위 댓글 목록 조회 (최신순 정렬)
     *
     * @param parentId 부모 댓글 ID
     * @return 하위 댓글 목록 (최신순 정렬)
     */
    List<Comment> findByParentIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long parentId);

    /**
     * 루트 댓글의 모든 후손(depth 2 + depth 3) 조회.
     *
     * 댓글 시스템은 최대 3단계 depth(1=댓글, 2=대댓글, 3=대대댓글)로 제한되므로
     * 직접 자식(depth 2)과 그 자식(depth 3)만 IN 서브쿼리로 한 번에 조회합니다.
     * 평탄 표시 + 멘션 prefix 패턴(인스타그램/오늘의집)에서 사용.
     *
     * @param rootId 루트 댓글 ID (depth 1)
     * @return 후손 댓글 목록 (createdAt 오름차순, 소프트 삭제 제외)
     */
    @Query("""
            SELECT c FROM Comment c
            WHERE c.deletedAt IS NULL
              AND (c.parentId = :rootId
                   OR c.parentId IN (
                     SELECT child.id FROM Comment child
                     WHERE child.parentId = :rootId AND child.deletedAt IS NULL
                   ))
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findAllDescendantsByRoot(@Param("rootId") Long rootId);
    
    /**
     * 게시글별 댓글 개수 조회 (소프트 삭제된 댓글 제외)
     * 
     * @param postId 게시글 ID
     * @return 댓글 개수
     */
    long countByPostIdAndDeletedAtIsNull(Long postId);
    
    /**
     * 게시글별 모든 댓글 조회 (소프트 삭제된 댓글 제외)
     * 
     * 게시글 삭제 시 모든 댓글을 소프트 삭제하기 위해 조회합니다.
     * 실제 삭제는 서비스 레이어에서 처리합니다.
     * 
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    List<Comment> findByPostIdAndDeletedAtIsNull(Long postId);
    
    /**
     * 댓글 존재 확인 (소프트 삭제된 댓글 제외)
     * 
     * @param id 댓글 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByIdAndDeletedAtIsNull(Long id);
    
    /**
     * 부모 댓글의 depth 조회
     * 
     * @param id 댓글 ID
     * @return depth (없으면 Optional.empty())
     */
    @Query("SELECT c.depth FROM Comment c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Integer> findDepthById(@Param("id") Long id);
    
    /**
     * 하위 댓글 존재 여부 확인
     * 
     * @param parentId 부모 댓글 ID
     * @return 하위 댓글이 있으면 true, 없으면 false
     */
    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);
    
    /**
     * 하위 댓글 개수 조회 (소프트 삭제된 댓글 제외)
     * 
     * @param parentId 부모 댓글 ID
     * @return 하위 댓글 개수
     */
    long countByParentIdAndDeletedAtIsNull(Long parentId);
}

