package org.cmarket.cmarket.web.profile.controller;

import jakarta.validation.Valid;
import org.cmarket.cmarket.domain.product.app.service.ProductService;
import org.cmarket.cmarket.domain.profile.app.service.ProfileService;
import org.cmarket.cmarket.web.auth.dto.UserWebDto;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.product.dto.FavoriteListResponse;
import org.cmarket.cmarket.web.product.dto.MyProductListResponse;
import org.cmarket.cmarket.web.profile.dto.BlockedUserListResponse;
import org.cmarket.cmarket.web.profile.dto.ProfileUpdateRequest;
import org.cmarket.cmarket.web.profile.dto.UserProfileResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 프로필 관련 컨트롤러
 * 
 * 프로필 조회, 수정 등 프로필 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    
    private final ProfileService profileService;
    private final ProductService productService;
    
    public ProfileController(ProfileService profileService, ProductService productService) {
        this.profileService = profileService;
        this.productService = productService;
    }
    
    /**
     * 사용자 정보 조회
     * 
     * GET /api/profile/me
     * 
     * 현재 로그인한 사용자의 기본 정보를 조회합니다.
     * 
     * @return 사용자 정보
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> getUserInfo() {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.profile.app.dto.MyPageDto myPageDto = profileService.getUserInfo(email);
        
        // 앱 DTO → 웹 DTO 변환
        UserProfileResponse response = UserProfileResponse.fromMyPageDto(myPageDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 내가 찜한 상품 목록 조회
     * 
     * GET /api/profile/me/favorites
     * 
     * 현재 로그인한 사용자가 찜한 상품 목록을 조회합니다.
     * - 최신순 정렬
     * - 페이지네이션 지원 (기본값: page=0, size=20)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=20)
     * @return 찜한 상품 목록 (페이지네이션 포함)
     */
    @GetMapping("/me/favorites")
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
     * 내가 등록한 판매 상품 목록 조회
     * 
     * GET /api/profile/me/products
     * 
     * 현재 로그인한 사용자가 등록한 판매 상품 목록을 조회합니다.
     * - 최신순 정렬
     * - 페이지네이션 지원 (기본값: page=0, size=20)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=20)
     * @return 판매 상품 목록 (페이지네이션 포함)
     */
    @GetMapping("/me/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<MyProductListResponse>> getMySellProductList(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.MyProductListDto myProductListDto = 
                productService.getMySellProductList(pageable, email);
        
        // 앱 DTO → 웹 DTO 변환
        MyProductListResponse response = MyProductListResponse.fromDto(myProductListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 내가 등록한 판매 요청 목록 조회
     * 
     * GET /api/profile/me/purchase-requests
     * 
     * 현재 로그인한 사용자가 등록한 판매 요청 목록을 조회합니다.
     * - 최신순 정렬
     * - 페이지네이션 지원 (기본값: page=0, size=20)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=20)
     * @return 판매 요청 목록 (페이지네이션 포함)
     */
    @GetMapping("/me/purchase-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<MyProductListResponse>> getMyPurchaseRequestList(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.MyProductListDto myProductListDto = 
                productService.getMyPurchaseRequestList(pageable, email);
        
        // 앱 DTO → 웹 DTO 변환
        MyProductListResponse response = MyProductListResponse.fromDto(myProductListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 프로필 정보 수정
     * 
     * PATCH /api/profile/me
     * 
     * 현재 로그인한 사용자의 프로필 정보를 수정합니다.
     * - 닉네임, 지역, 프로필 이미지 URL, 소개글 수정 가능
     * 
     * @param request 프로필 수정 요청
     * @return 수정된 사용자 정보
     */
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<UserWebDto>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 웹 DTO → 앱 DTO 변환
        org.cmarket.cmarket.domain.profile.app.dto.ProfileUpdateCommand command =
                org.cmarket.cmarket.domain.profile.app.dto.ProfileUpdateCommand.builder()
                        .email(email)
                        .nickname(request.getNickname())
                        .addressSido(request.getAddressSido())
                        .addressGugun(request.getAddressGugun())
                        .profileImageUrl(request.getProfileImageUrl())
                        .introduction(request.getIntroduction())
                        .build();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.auth.app.dto.UserDto userDto = profileService.updateProfile(command);
        
        // 앱 DTO → 웹 DTO 변환
        UserWebDto userWebDto = UserWebDto.fromDto(userDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, userWebDto));
    }
    
    /**
     * 유저 프로필 조회
     * 
     * GET /api/profile/{userId}
     * 
     * 특정 사용자의 프로필 정보를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 유저 프로필 정보
     */
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<UserProfileResponse>> getUserProfile(
            @PathVariable Long userId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출 (차단 여부 확인 포함)
        org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto userProfileDto = 
                profileService.getUserProfile(userId, email);
        
        // 앱 DTO → 웹 DTO 변환
        UserProfileResponse response = UserProfileResponse.fromDto(userProfileDto, userProfileDto.getIsBlocked());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 다른 유저가 등록한 판매 상품 목록 조회
     * 
     * GET /api/profile/{userId}/products
     * 
     * 특정 사용자가 등록한 판매 상품 목록을 조회합니다.
     * - 최신순 정렬
     * - 페이지네이션 지원 (기본값: page=0, size=20)
     * - 판매 상품(SELL)만 조회되며, 판매 요청(REQUEST)은 제외됩니다.
     * 
     * @param userId 조회할 사용자 ID
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=20)
     * @return 판매 상품 목록 (페이지네이션 포함)
     */
    @GetMapping("/{userId}/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<MyProductListResponse>> getUserSellProductList(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.product.app.dto.MyProductListDto myProductListDto = 
                productService.getUserSellProductList(userId, pageable, email);
        
        // 앱 DTO → 웹 DTO 변환
        MyProductListResponse response = MyProductListResponse.fromDto(myProductListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 차단한 유저 목록 조회
     * 
     * GET /api/profile/me/blocked-users
     * 
     * 현재 로그인한 사용자가 차단한 유저 목록을 조회합니다.
     * - 최신순 정렬 (createdAt DESC)
     * - 페이지네이션 지원 (기본값: page=0, size=10)
     * 
     * @param pageable 페이지네이션 정보 (기본값: page=0, size=10)
     * @return 차단한 유저 목록 (페이지네이션 포함)
     */
    @GetMapping("/me/blocked-users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<BlockedUserListResponse>> getBlockedUsers(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.profile.app.dto.BlockedUserListDto blockedUserListDto = 
                profileService.getBlockedUsers(email, pageable);
        
        // 앱 DTO → 웹 DTO 변환
        BlockedUserListResponse response = BlockedUserListResponse.fromDto(blockedUserListDto);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
    
    /**
     * 유저 차단 해제
     * 
     * DELETE /api/profile/me/blocked-users/{blockedUserId}
     * 
     * 현재 로그인한 사용자가 차단한 유저를 차단 해제합니다.
     * 차단 관계가 존재하지 않는 경우에도 성공으로 처리합니다 (idempotent).
     * 
     * @param blockedUserId 차단 해제할 사용자 ID
     * @return 성공 응답
     */
    @DeleteMapping("/me/blocked-users/{blockedUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<Void>> unblockUser(
            @PathVariable Long blockedUserId
    ) {
        // 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 앱 서비스 호출
        profileService.unblockUser(email, blockedUserId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, null));
    }
}

