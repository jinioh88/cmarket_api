package org.cmarket.cmarket.domain.product.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 상품 엔티티
 * 
 * 판매 상품과 판매 요청을 모두 관리하는 엔티티입니다.
 * - productType으로 판매 상품(SELL)과 판매 요청(REQUEST)을 구분
 * - 소프트 삭제 지원 (deletedAt)
 * - 조회수와 찜 개수 관리
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_seller_id", columnList = "seller_id"),
        @Index(name = "idx_product_type", columnList = "product_type"),
        @Index(name = "idx_trade_status", columnList = "trade_status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "seller_id")
    private Long sellerId;  // 판매자 ID (User 참조)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_type", length = 20)
    private ProductType productType;  // 판매중/판매요청
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "pet_type", length = 20)
    private PetType petType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "pet_detail_type", length = 30)
    private PetDetailType petDetailType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;
    
    @Column(nullable = false, length = 50)
    private String title;  // 상품명 (2-50자)
    
    @Column(length = 1000)
    private String description;  // 상품 설명 (최대 1000자)
    
    @Column(nullable = false)
    private Long price;  // 가격 (판매 상품: 판매 가격, 판매 요청: 희망 가격)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_status", length = 20)
    private ProductStatus productStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "trade_status", length = 20)
    private TradeStatus tradeStatus;
    
    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;  // 대표 이미지 URL
    
    @ElementCollection
    @CollectionTable(
        name = "product_sub_images",
        joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url", length = 500)
    private List<String> subImageUrls = new ArrayList<>();  // 서브 이미지 URL 리스트 (최대 4장)
    
    @Column(name = "address_sido", length = 50)
    private String addressSido;  // 거래 희망 지역 (시/도)
    
    @Column(name = "address_gugun", length = 50)
    private String addressGugun;  // 거래 희망 지역 (구/군)
    
    @Column(nullable = false, name = "is_delivery_available")
    private Boolean isDeliveryAvailable;  // 택배 거래 가능 여부
    
    @Column(name = "preferred_meeting_place", length = 200)
    private String preferredMeetingPlace;  // 선호하는 만남 장소
    
    @Column(nullable = false, name = "view_count")
    private Long viewCount = 0L;  // 조회수 (기본값 0)
    
    @Column(nullable = false, name = "favorite_count")
    private Long favoriteCount = 0L;  // 찜 개수 (기본값 0)
    
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // 소프트 삭제용 (null이면 활성, 값이 있으면 삭제됨)
    
    @Builder
    public Product(
            Long sellerId,
            ProductType productType,
            PetType petType,
            PetDetailType petDetailType,
            Category category,
            String title,
            String description,
            Long price,
            ProductStatus productStatus,
            TradeStatus tradeStatus,
            String mainImageUrl,
            List<String> subImageUrls,
            String addressSido,
            String addressGugun,
            Boolean isDeliveryAvailable,
            String preferredMeetingPlace
    ) {
        this.sellerId = sellerId;
        this.productType = productType;
        this.petType = petType;
        this.petDetailType = petDetailType;
        this.category = category;
        this.title = title;
        this.description = description;
        this.price = price;
        this.productStatus = productStatus;
        this.tradeStatus = tradeStatus;
        this.mainImageUrl = mainImageUrl;
        this.subImageUrls = subImageUrls != null ? new ArrayList<>(subImageUrls) : new ArrayList<>();
        this.addressSido = addressSido;
        this.addressGugun = addressGugun;
        this.isDeliveryAvailable = isDeliveryAvailable != null ? isDeliveryAvailable : false;
        this.preferredMeetingPlace = preferredMeetingPlace;
        this.viewCount = 0L;
        this.favoriteCount = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 상품 정보 수정
     */
    public void update(
            PetType petType,
            PetDetailType petDetailType,
            Category category,
            String title,
            String description,
            Long price,
            ProductStatus productStatus,
            String mainImageUrl,
            List<String> subImageUrls,
            Boolean isDeliveryAvailable,
            String addressSido,
            String addressGugun,
            String preferredMeetingPlace
    ) {
        this.petType = petType;
        this.petDetailType = petDetailType;
        this.category = category;
        this.title = title;
        this.description = description;
        this.price = price;
        this.productStatus = productStatus;
        this.mainImageUrl = mainImageUrl;
        this.subImageUrls = subImageUrls != null ? new ArrayList<>(subImageUrls) : new ArrayList<>();
        this.isDeliveryAvailable = isDeliveryAvailable != null ? isDeliveryAvailable : false;
        this.addressSido = addressSido;
        this.addressGugun = addressGugun;
        this.preferredMeetingPlace = preferredMeetingPlace;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 거래 상태 변경
     */
    public void updateTradeStatus(TradeStatus tradeStatus) {
        this.tradeStatus = tradeStatus;
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
     * 찜 개수 증가
     */
    public void increaseFavoriteCount() {
        this.favoriteCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 찜 개수 감소
     */
    public void decreaseFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
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

