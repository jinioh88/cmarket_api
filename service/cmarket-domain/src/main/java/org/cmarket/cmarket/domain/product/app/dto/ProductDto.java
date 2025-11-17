package org.cmarket.cmarket.domain.product.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductStatus;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 정보 DTO
 * 
 * 앱 서비스에서 사용하는 상품 정보 DTO입니다.
 */
@Getter
@Builder
public class ProductDto {
    private Long id;
    private Long sellerId;
    private ProductType productType;
    private PetType petType;
    private PetDetailType petDetailType;
    private Category category;
    private String title;
    private String description;
    private Long price;
    private ProductStatus productStatus;
    private TradeStatus tradeStatus;
    private String mainImageUrl;
    private List<String> subImageUrls;
    private String addressSido;
    private String addressGugun;
    private Boolean isDeliveryAvailable;
    private String preferredMeetingPlace;
    private Long viewCount;
    private Long favoriteCount;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Product 엔티티를 ProductDto로 변환
     * 
     * @param product Product 엔티티
     * @param isFavorite 찜 여부
     * @return ProductDto
     */
    public static ProductDto fromEntity(Product product, Boolean isFavorite) {
        return ProductDto.builder()
                .id(product.getId())
                .sellerId(product.getSellerId())
                .productType(product.getProductType())
                .petType(product.getPetType())
                .petDetailType(product.getPetDetailType())
                .category(product.getCategory())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .productStatus(product.getProductStatus())
                .tradeStatus(product.getTradeStatus())
                .mainImageUrl(product.getMainImageUrl())
                .subImageUrls(product.getSubImageUrls())
                .addressSido(product.getAddressSido())
                .addressGugun(product.getAddressGugun())
                .isDeliveryAvailable(product.getIsDeliveryAvailable())
                .preferredMeetingPlace(product.getPreferredMeetingPlace())
                .viewCount(product.getViewCount())
                .favoriteCount(product.getFavoriteCount())
                .isFavorite(isFavorite != null ? isFavorite : false)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    /**
     * Product 엔티티를 ProductDto로 변환 (isFavorite 없이)
     * 
     * @param product Product 엔티티
     * @return ProductDto
     */
    public static ProductDto fromEntity(Product product) {
        return fromEntity(product, null);
    }
}


