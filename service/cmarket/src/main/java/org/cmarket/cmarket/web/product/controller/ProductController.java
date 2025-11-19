package org.cmarket.cmarket.web.product.controller;

import jakarta.validation.Valid;
import org.cmarket.cmarket.domain.product.app.service.ProductService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.product.dto.FavoriteListResponse;
import org.cmarket.cmarket.web.product.dto.ProductCreateRequest;
import org.cmarket.cmarket.web.product.dto.ProductDetailResponse;
import org.cmarket.cmarket.web.product.dto.MyProductListResponse;
import org.cmarket.cmarket.web.product.dto.ProductRequestCreateRequest;
import org.cmarket.cmarket.web.product.dto.ProductRequestDetailResponse;
import org.cmarket.cmarket.web.product.dto.ProductRequestUpdateRequest;
import org.cmarket.cmarket.web.product.dto.ProductResponse;
import org.cmarket.cmarket.web.product.dto.ProductUpdateRequest;
import org.cmarket.cmarket.web.product.dto.TradeStatusUpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 관련 컨트롤러
 * 
 * 상품 등록, 조회 등 상품 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * 판매 상품 등록
     * 
     * POST /api/products
     * 
     * 현재 로그인한 사용자가 판매 상품을 등록합니다.
     * 
     * @param request 상품 등록 요청
     * @return 생성된 상품 정보
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.product.app.dto.ProductCreateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductDto productDto = productService.createProduct(email, command);
        
        // 앱 DTO → 웹 DTO 변환
        ProductResponse response = ProductResponse.fromDto(productDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(ResponseCode.CREATED, response));
    }
    
    /**
     * 판매 상품 상세 조회
     * 
     * GET /api/products/{productId}
     * 
     * 등록된 판매 상품의 상세 정보를 조회합니다.
     * - 상품 상세 정보
     * - 판매자 정보
     * - 판매자의 다른 상품 목록 (최대 5개)
     * - 로그인한 사용자는 찜 여부 표시
     * 
     * @param productId 상품 ID
     * @return 상품 상세 정보
     */
    @GetMapping("/{productId}")
    public ResponseEntity<SuccessResponse<ProductDetailResponse>> getProductDetail(
            @PathVariable Long productId
    ) {
        // 현재 로그인한 사용자 이메일 추출 (선택적, 비로그인 시 null)
        String email = null;
        if (SecurityUtils.isAuthenticated()) {
            email = SecurityUtils.getCurrentUserEmail();
        }
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductDetailDto productDetailDto = 
                productService.getProductDetail(productId, email);
        
        // 앱 DTO → 웹 DTO 변환
        ProductDetailResponse response = ProductDetailResponse.fromDto(productDetailDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 판매 상품 수정
     * 
     * PATCH /api/products/{productId}
     * 
     * 등록된 판매 상품의 정보를 수정합니다.
     * - 판매자 본인만 수정 가능
     * - 수정 가능한 항목: 반려동물 종류, 카테고리, 제목, 설명, 가격, 상품 상태, 이미지, 거래 희망 지역 등
     * 
     * @param productId 상품 ID
     * @param request 수정 요청 DTO
     * @return 수정된 상품 정보
     */
    @PatchMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.product.app.dto.ProductUpdateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductDto productDto = 
                productService.updateProduct(productId, command, email);
        
        // 앱 DTO → 웹 DTO 변환
        ProductResponse response = ProductResponse.fromDto(productDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 거래 상태 변경
     * 
     * PATCH /api/products/{productId}/trade-status
     * 
     * 등록된 판매 상품 또는 판매 요청의 거래 상태를 변경합니다.
     * - 판매자 본인만 변경 가능
     * - 판매 상품: SELLING → RESERVED → COMPLETED
     * - 판매 요청: BUYING → RESERVED → COMPLETED
     * 
     * @param productId 상품 ID
     * @param request 거래 상태 변경 요청 DTO
     * @return 수정된 상품 정보
     */
    @PatchMapping("/{productId}/trade-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ProductResponse>> updateTradeStatus(
            @PathVariable Long productId,
            @Valid @RequestBody TradeStatusUpdateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.product.app.dto.TradeStatusUpdateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductDto productDto = 
                productService.updateTradeStatus(productId, command, email);
        
        // 앱 DTO → 웹 DTO 변환
        ProductResponse response = ProductResponse.fromDto(productDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 판매 상품/판매요청 삭제
     * 
     * DELETE /api/products/{productId}
     * 
     * 등록된 판매 상품 또는 판매 요청을 삭제합니다.
     * - 판매자 본인만 삭제 가능
     * - 소프트 삭제 처리 (deletedAt 설정)
     * - 삭제 후 복구 불가능
     * 
     * @param productId 상품 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> deleteProduct(
            @PathVariable Long productId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        productService.deleteProduct(productId, email);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
    
    /**
     * 관심 목록 추가/삭제 (토글)
     * 
     * POST /api/products/{productId}/favorite
     * 
     * 상품을 관심 목록에 추가하거나 삭제합니다.
     * - 이미 찜한 경우: 찜 삭제, favoriteCount 감소
     * - 찜하지 않은 경우: 찜 추가, favoriteCount 증가
     * 
     * @param productId 상품 ID
     * @return 수정된 상품 정보 (isFavorite 포함)
     */
    @PostMapping("/{productId}/favorite")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ProductResponse>> toggleFavorite(
            @PathVariable Long productId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductDto productDto = 
                productService.toggleFavorite(productId, email);
        
        // 앱 DTO → 웹 DTO 변환
        ProductResponse response = ProductResponse.fromDto(productDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 관심 목록 조회
     * 
     * GET /api/products/favorites
     * 
     * 현재 로그인한 사용자가 찜한 상품 목록을 조회합니다.
     * - 최신순 정렬
     * - 페이지네이션 지원 (기본값: page=0, size=20)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=20)
     * @return 관심 목록 (페이지네이션 포함)
     */
    @GetMapping("/favorites")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<FavoriteListResponse>> getFavoriteList(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.FavoriteListDto favoriteListDto = 
                productService.getFavoriteList(pageable, email);
        
        // 앱 DTO → 웹 DTO 변환
        FavoriteListResponse response = FavoriteListResponse.fromDto(favoriteListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 판매 요청 등록
     * 
     * POST /api/products/requests
     * 
     * 필요한 물품을 요청하는 게시글을 작성합니다.
     * - 기본정보: 반려동물 종류, 상품 카테고리, 상품명, 상세 요청사항
     * - 희망가격 및 상태: 희망 가격
     * - 이미지: 대표 이미지 1장, 서브 이미지 최대 4장
     * - 거래정보: 거래 희망 지역
     * 
     * @param request 판매 요청 등록 요청 DTO
     * @return 등록된 판매 요청 정보
     */
    @PostMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ProductResponse>> createProductRequest(
            @Valid @RequestBody ProductRequestCreateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.product.app.dto.ProductRequestCreateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductDto productDto = 
                productService.createProductRequest(email, command);
        
        // 앱 DTO → 웹 DTO 변환
        ProductResponse response = ProductResponse.fromDto(productDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 판매 요청 상세 조회
     * 
     * GET /api/products/requests/{productId}
     * 
     * 등록된 판매 요청의 상세 정보를 조회합니다.
     * - 판매 요청 상세 정보
     * - 게시자 정보
     * - 게시자의 다른 상품 목록 (최대 5개)
     * - 로그인한 사용자는 찜 여부 표시
     * 
     * @param productId 상품 ID
     * @return 판매 요청 상세 정보
     */
    @GetMapping("/requests/{productId}")
    public ResponseEntity<SuccessResponse<ProductRequestDetailResponse>> getProductRequestDetail(
            @PathVariable Long productId
    ) {
        // 현재 로그인한 사용자 이메일 추출 (선택적, 비로그인 시 null)
        String email = null;
        if (SecurityUtils.isAuthenticated()) {
            email = SecurityUtils.getCurrentUserEmail();
        }
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductRequestDetailDto productRequestDetailDto = 
                productService.getProductRequestDetail(productId, email);
        
        // 앱 DTO → 웹 DTO 변환
        ProductRequestDetailResponse response = ProductRequestDetailResponse.fromDto(productRequestDetailDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 판매 요청 수정
     * 
     * PATCH /api/products/requests/{productId}
     * 
     * 등록된 판매 요청의 정보를 수정합니다.
     * - 게시자 본인만 수정 가능
     * - 수정 가능한 항목: 반려동물 종류, 카테고리, 제목, 설명, 희망 가격, 이미지, 거래 희망 지역 등
     * 
     * @param productId 상품 ID
     * @param request 수정 요청 DTO
     * @return 수정된 판매 요청 정보
     */
    @PatchMapping("/requests/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<ProductResponse>> updateProductRequest(
            @PathVariable Long productId,
            @Valid @RequestBody ProductRequestUpdateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.product.app.dto.ProductRequestUpdateCommand command = request.toCommand();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.ProductDto productDto = 
                productService.updateProductRequest(productId, command, email);
        
        // 앱 DTO → 웹 DTO 변환
        ProductResponse response = ProductResponse.fromDto(productDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 내가 등록한 상품 목록 조회
     * 
     * GET /api/products/me
     * 
     * 현재 로그인한 사용자가 등록한 판매 상품과 판매 요청 목록을 조회합니다.
     * - 판매 상품과 판매 요청 모두 포함
     * - 최신순 정렬
     * - 페이지네이션 지원 (기본값: page=0, size=20)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=20)
     * @return 내가 등록한 상품 목록 (페이지네이션 포함)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<MyProductListResponse>> getMyProductList(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.MyProductListDto myProductListDto = 
                productService.getMyProductList(pageable, email);
        
        // 앱 DTO → 웹 DTO 변환
        MyProductListResponse response = MyProductListResponse.fromDto(myProductListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}


