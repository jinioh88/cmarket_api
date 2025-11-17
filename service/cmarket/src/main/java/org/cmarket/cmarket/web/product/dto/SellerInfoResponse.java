package org.cmarket.cmarket.web.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.product.app.dto.SellerInfoDto;

/**
 * 판매자 정보 응답 DTO
 * 
 * 상품 상세 조회에서 사용하는 판매자 정보 웹 DTO입니다.
 */
@Getter
@NoArgsConstructor
public class SellerInfoResponse {
    private Long sellerId;
    private String sellerNickname;
    private String sellerProfileImageUrl;
    
    /**
     * 앱 DTO를 웹 DTO로 변환
     * 
     * @param dto SellerInfoDto
     * @return SellerInfoResponse
     */
    public static SellerInfoResponse fromDto(SellerInfoDto dto) {
        SellerInfoResponse response = new SellerInfoResponse();
        response.sellerId = dto.getSellerId();
        response.sellerNickname = dto.getSellerNickname();
        response.sellerProfileImageUrl = dto.getSellerProfileImageUrl();
        return response;
    }
}

