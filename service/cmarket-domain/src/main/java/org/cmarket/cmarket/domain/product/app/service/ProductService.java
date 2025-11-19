package org.cmarket.cmarket.domain.product.app.service;

import org.cmarket.cmarket.domain.product.app.dto.ProductCreateCommand;
import org.cmarket.cmarket.domain.product.app.dto.ProductDto;

/**
 * 상품 서비스 인터페이스
 * 
 * 상품 관련 비즈니스 로직을 담당합니다.
 */
public interface ProductService {
    
    /**
     * 판매 상품 등록
     * 
     * @param email 현재 로그인한 사용자 이메일
     * @param command 상품 등록 명령
     * @return 생성된 상품 정보
     */
    ProductDto createProduct(String email, ProductCreateCommand command);
    
    /**
     * 판매 상품 상세 조회
     * 
     * @param productId 상품 ID
     * @param email 현재 로그인한 사용자 이메일 (비로그인 시 null)
     * @return 상품 상세 정보
     */
    org.cmarket.cmarket.domain.product.app.dto.ProductDetailDto getProductDetail(
            Long productId,
            String email
    );
    
    /**
     * 상품 조회수 증가
     * 
     * @param productId 상품 ID
     * @param email 현재 로그인한 사용자 이메일
     */
    void increaseViewCount(Long productId, String email);
    
    /**
     * 판매 상품 수정
     * 
     * @param productId 상품 ID
     * @param command 수정 명령 DTO
     * @param email 현재 로그인한 사용자 이메일
     * @return 수정된 상품 정보
     */
    org.cmarket.cmarket.domain.product.app.dto.ProductDto updateProduct(
            Long productId,
            org.cmarket.cmarket.domain.product.app.dto.ProductUpdateCommand command,
            String email
    );
    
    /**
     * 거래 상태 변경
     * 
     * @param productId 상품 ID
     * @param command 거래 상태 변경 명령 DTO
     * @param email 현재 로그인한 사용자 이메일
     * @return 수정된 상품 정보
     */
    org.cmarket.cmarket.domain.product.app.dto.ProductDto updateTradeStatus(
            Long productId,
            org.cmarket.cmarket.domain.product.app.dto.TradeStatusUpdateCommand command,
            String email
    );
    
    /**
     * 판매 상품/판매요청 삭제
     * 
     * @param productId 상품 ID
     * @param email 현재 로그인한 사용자 이메일
     */
    void deleteProduct(Long productId, String email);
    
    /**
     * 관심 목록 추가/삭제 (토글)
     * 
     * @param productId 상품 ID
     * @param email 현재 로그인한 사용자 이메일
     * @return 수정된 상품 정보 (isFavorite 포함)
     */
    org.cmarket.cmarket.domain.product.app.dto.ProductDto toggleFavorite(Long productId, String email);
    
    /**
     * 관심 목록 조회
     * 
     * @param pageable 페이지네이션 정보
     * @param email 현재 로그인한 사용자 이메일
     * @return 관심 목록 조회 결과
     */
    org.cmarket.cmarket.domain.product.app.dto.FavoriteListDto getFavoriteList(
            org.springframework.data.domain.Pageable pageable,
            String email
    );
    
    /**
     * 판매 요청 등록
     * 
     * @param email 현재 로그인한 사용자 이메일
     * @param command 판매 요청 등록 명령 DTO
     * @return 등록된 판매 요청 정보
     */
    org.cmarket.cmarket.domain.product.app.dto.ProductDto createProductRequest(
            String email,
            org.cmarket.cmarket.domain.product.app.dto.ProductRequestCreateCommand command
    );
    
    /**
     * 판매 요청 상세 조회
     * 
     * @param productId 상품 ID
     * @param email 현재 로그인한 사용자 이메일 (비로그인 시 null)
     * @return 판매 요청 상세 정보
     */
    org.cmarket.cmarket.domain.product.app.dto.ProductRequestDetailDto getProductRequestDetail(
            Long productId,
            String email
    );
    
    /**
     * 판매 요청 수정
     * 
     * @param productId 상품 ID
     * @param command 수정 명령 DTO
     * @param email 현재 로그인한 사용자 이메일
     * @return 수정된 판매 요청 정보
     */
    org.cmarket.cmarket.domain.product.app.dto.ProductDto updateProductRequest(
            Long productId,
            org.cmarket.cmarket.domain.product.app.dto.ProductRequestUpdateCommand command,
            String email
    );
    
    /**
     * 내가 등록한 상품 목록 조회
     * 
     * @param pageable 페이지네이션 정보
     * @param email 현재 로그인한 사용자 이메일
     * @return 내가 등록한 상품 목록 조회 결과
     */
    org.cmarket.cmarket.domain.product.app.dto.MyProductListDto getMyProductList(
            org.springframework.data.domain.Pageable pageable,
            String email
    );
}

