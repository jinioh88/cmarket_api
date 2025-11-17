package org.cmarket.cmarket.domain.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 관심 목록(찜) 엔티티
 * 
 * 사용자가 상품을 찜한 정보를 저장합니다.
 * - userId와 productId의 복합 unique 제약조건으로 중복 찜 방지
 * - userId와 productId에 인덱스 추가하여 조회 성능 향상
 */
@Entity
@Table(
    name = "favorites",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_product",
            columnNames = {"user_id", "product_id"}
        )
    },
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_product_id", columnList = "product_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "user_id")
    private Long userId;  // 찜한 사용자 ID
    
    @Column(nullable = false, name = "product_id")
    private Long productId;  // 상품 ID
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Builder
    public Favorite(
            Long userId,
            Long productId
    ) {
        this.userId = userId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
    }
}

