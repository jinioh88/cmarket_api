package org.cmarket.cmarket.domain.product.repository;

import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Product 엔티티 레포지토리 인터페이스
 * 
 * Spring Data JPA가 자동으로 구현체를 생성합니다.
 * 메서드 이름 규칙을 따르면 쿼리가 자동 생성됩니다.
 * 
 * 주요 기능:
 * - 판매자별 상품 목록 조회
 * - 판매 상품/판매 요청 목록 조회
 * - 상품 존재 확인
 * - 소프트 삭제된 상품 제외 조회
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 판매자별 상품 목록 조회 (판매 상품 + 판매 요청 모두 포함, 페이지네이션, 최신순 정렬)
     * 
     * @param sellerId 판매자 ID
     * @param pageable 페이지네이션 정보
     * @return 상품 목록 (최신순 정렬)
     */
    Page<Product> findBySellerIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    
    /**
     * 상품 타입별 목록 조회 (판매 상품 또는 판매 요청, 페이지네이션, 최신순 정렬)
     * 
     * @param productType 상품 타입 (SELL: 판매 상품, REQUEST: 판매 요청)
     * @param pageable 페이지네이션 정보
     * @return 상품 목록 (최신순 정렬)
     */
    Page<Product> findByProductTypeAndDeletedAtIsNullOrderByCreatedAtDesc(ProductType productType, Pageable pageable);
    
    /**
     * 판매자별 다른 상품 목록 조회 (상세 페이지용, 현재 상품 제외)
     * 
     * @param sellerId 판매자 ID
     * @param excludeProductId 제외할 상품 ID (현재 조회 중인 상품)
     * @param pageable 페이지네이션 정보
     * @return 상품 목록 (최신순 정렬. 5개씩 조회)
     */
    List<Product> findBySellerIdAndIdNotAndDeletedAtIsNullOrderByCreatedAtDesc(Long sellerId, Long excludeProductId, Pageable pageable);
    
    /**
     * 상품 조회 (소프트 삭제된 상품 제외)
     * 
     * @param id 상품 ID
     * @return 상품 (없으면 Optional.empty())
     */
    Optional<Product> findByIdAndDeletedAtIsNull(Long id);
}

