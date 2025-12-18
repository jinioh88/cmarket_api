package org.cmarket.cmarket.domain.chat.model;

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
 * 채팅방 엔티티
 * 
 * 채팅방 정보를 저장하는 도메인 모델입니다.
 * - 상품 정보는 채팅방 생성 시점의 스냅샷으로 저장
 * - 상품이 삭제/수정되어도 채팅방의 상품 정보는 유지
 */
@Entity
@Table(
    name = "chat_rooms",
    indexes = {
        @Index(name = "idx_chat_room_product_id", columnList = "product_id"),
        @Index(name = "idx_chat_room_created_at", columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "product_id")
    private Long productId;  // 상품 ID (Product 참조)
    
    @Column(nullable = false, name = "product_title", length = 50)
    private String productTitle;  // 상품 제목 (생성 시점 스냅샷)
    
    @Column(nullable = false, name = "product_price")
    private Long productPrice;  // 상품 가격 (생성 시점 스냅샷)
    
    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;  // 상품 대표 이미지 (생성 시점 스냅샷)
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    public ChatRoom(
            Long productId,
            String productTitle,
            Long productPrice,
            String productImageUrl
    ) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.productPrice = productPrice;
        this.productImageUrl = productImageUrl;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 채팅방 업데이트 시간 갱신
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
