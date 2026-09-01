package org.cmarket.cmarket.domain.product.repository;

import org.cmarket.cmarket.domain.product.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Favorite 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 찜 여부 확인
 * - 찜 삭제
 * - 사용자별 찜 목록 조회
 * - 상품별 찜 개수 조회
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * 찜 여부 확인
     * 
     * @param userId 사용자 ID
     * @param productId 상품 ID
     * @return 찜했으면 true, 아니면 false
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 찜 삭제
     * 
     * @param userId 사용자 ID
     * @param productId 상품 ID
     */
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 사용자별 찜 목록 조회 (페이지네이션, 최신순 정렬)
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 찜 목록 (최신순 정렬)
     */
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 사용자별 찜 목록 조회 — 삭제된 상품은 빼고 (페이지네이션, 최신순 정렬)
     * 
     * 위 findByUserIdOrderByCreatedAtDesc 는 찜 테이블만 본다. 그래서 찜해 둔 상품이
     * 소프트 삭제되면 그 찜도 함께 딸려 나오고, 서비스에서 상품을 못 찾아 걸러내면
     * 목록은 비는데 total 은 그대로 남는다.
     * 화면에는 「찜한 상품이 없습니다」 아래에 「상품 1」 이 뜬다.
     * 
     * 그래서 걸러내기를 서비스가 아니라 쿼리로 옮긴다. 목록과 개수가 같은 조건에서
     * 나오므로 여러 페이지여도 어긋나지 않는다.
     * 
     * ⚠️ Favorite 은 Product 와 연관관계가 없고 productId 만 들고 있다. 그래서
     *    join 이 아니라 서브쿼리로 거른다.
     * ⚠️ countQuery 를 직접 적는다. 자동 생성에 맡기지 않는 것은, 이 쿼리에서 개수가
     *    어긋난 것이 바로 이 버그이기 때문이다 — 두 쿼리의 조건을 눈으로 맞춰 둔다.
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지네이션 정보
     * @return 살아 있는 상품의 찜 목록 (최신순 정렬)
     */
    @Query(
            value = "SELECT f FROM Favorite f "
                    + "WHERE f.userId = :userId "
                    + "AND f.productId IN (SELECT p.id FROM Product p WHERE p.deletedAt IS NULL) "
                    + "ORDER BY f.createdAt DESC",
            countQuery = "SELECT COUNT(f) FROM Favorite f "
                    + "WHERE f.userId = :userId "
                    + "AND f.productId IN (SELECT p.id FROM Product p WHERE p.deletedAt IS NULL)"
    )
    Page<Favorite> findAliveByUserIdOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable
    );
    
    /**
     * 상품별 찜 개수 조회
     * 
     * @param productId 상품 ID
     * @return 찜 개수
     */
    long countByProductId(Long productId);
    
    /**
     * 사용자가 찜한 상품 ID 목록 조회
     * 
     * N+1 문제 방지를 위해 한 번의 쿼리로 찜한 상품 ID 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param productIds 조회할 상품 ID 목록
     * @return 찜한 상품 ID 목록
     */
    @Query("SELECT f.productId FROM Favorite f WHERE f.userId = :userId AND f.productId IN :productIds")
    java.util.List<Long> findProductIdsByUserIdAndProductIdIn(
            @Param("userId") Long userId,
            @Param("productIds") java.util.List<Long> productIds
    );
    
    /**
     * 상품을 찜한 사용자 ID 목록 조회
     * 
     * 특정 상품을 찜한 모든 사용자의 ID를 조회합니다.
     * 알림 발행 시 사용됩니다.
     * 
     * @param productId 상품 ID
     * @return 찜한 사용자 ID 목록
     */
    @Query("SELECT f.userId FROM Favorite f WHERE f.productId = :productId")
    java.util.List<Long> findUserIdsByProductId(@Param("productId") Long productId);
}

