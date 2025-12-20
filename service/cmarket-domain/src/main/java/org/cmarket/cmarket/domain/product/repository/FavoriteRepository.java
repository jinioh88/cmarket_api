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

