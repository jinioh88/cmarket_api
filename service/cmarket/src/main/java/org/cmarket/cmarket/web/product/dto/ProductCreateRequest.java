package org.cmarket.cmarket.web.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.ProductCreateCommand;
import org.cmarket.cmarket.domain.product.model.Category;
import org.cmarket.cmarket.domain.product.model.PetDetailType;
import org.cmarket.cmarket.domain.product.model.PetType;
import org.cmarket.cmarket.domain.product.model.ProductStatus;

import java.util.List;

/**
 * 상품 등록 요청 DTO
 * 
 * 상품 등록 시 필요한 모든 정보를 받습니다.
 */
@Getter
@NoArgsConstructor
public class ProductCreateRequest {
    
    @NotNull(message = "동물 종류는 필수입니다.")
    private PetType petType;
    
    @NotNull(message = "반려동물 상세 종류는 필수입니다.")
    private PetDetailType petDetailType;
    
    @NotNull(message = "상품 카테고리는 필수입니다.")
    private Category category;
    
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(min = 2, max = 50, message = "상품명은 2자 이상 50자 이하여야 합니다.")
    private String title;
    
    @Size(max = 1000, message = "상품 설명은 최대 1000자까지 입력 가능합니다.")
    private String description;
    
    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Long price;
    
    @NotNull(message = "상품 상태는 필수입니다.")
    private ProductStatus productStatus;
    
//    @NotBlank(message = "대표 이미지는 필수입니다.")
    private String mainImageUrl;
    
    @Size(max = 4, message = "서브 이미지는 최대 4장까지 등록 가능합니다.")
    private List<String> subImageUrls;
    
    @NotBlank(message = "시/도는 필수입니다.")
    private String addressSido;
    
    @NotBlank(message = "구/군은 필수입니다.")
    private String addressGugun;
    
    /**
     * 웹 DTO를 앱 DTO로 변환
     * 
     * @return ProductCreateCommand
     */
    public ProductCreateCommand toCommand() {
        return ProductCreateCommand.builder()
                .petType(this.petType)
                .petDetailType(this.petDetailType)
                .category(this.category)
                .title(this.title)
                .description(this.description)
                .price(this.price)
                .productStatus(this.productStatus)
                .mainImageUrl(this.mainImageUrl)
                .subImageUrls(this.subImageUrls)
                .addressSido(this.addressSido)
                .addressGugun(this.addressGugun)
                .build();
    }
}

