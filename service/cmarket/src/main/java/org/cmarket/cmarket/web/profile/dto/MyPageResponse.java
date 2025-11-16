package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 마이페이지 정보 웹 DTO
 * 
 * 웹 계층에서 사용하는 마이페이지 정보 DTO입니다.
 */
@Getter
@Builder
public class MyPageResponse {
    private String profileImageUrl;
    private String nickname;
    private String name;
    private String introduction;
    private LocalDate birthDate;
    private String email;
    private String addressSido;
    private String addressGugun;
    private LocalDateTime createdAt;
    
    // 찜한 상품 목록 (향후 Product 도메인에서 구현 예정)
    private List<Object> favoriteProducts;  // 빈 리스트로 반환
    
    // 등록한 상품 목록 (향후 Product 도메인에서 구현 예정)
    private List<Object> myProducts;  // 빈 리스트로 반환
    
    // 판매 요청 목록 (향후 Product 도메인에서 구현 예정)
    private List<Object> purchaseRequests;  // 빈 리스트로 반환
    
    // 차단한 유저 목록
    private List<BlockedUserResponse> blockedUsers;
    
    /**
     * 앱 DTO에서 웹 DTO로 변환
     * 
     * @param myPageDto 앱 DTO
     * @return 웹 DTO
     */
    public static MyPageResponse fromDto(org.cmarket.cmarket.domain.profile.app.dto.MyPageDto myPageDto) {
        List<BlockedUserResponse> blockedUserResponses = myPageDto.getBlockedUsers().stream()
                .map(blockedUserDto -> BlockedUserResponse.builder()
                        .blockedUserId(blockedUserDto.getBlockedUserId())
                        .nickname(blockedUserDto.getNickname())
                        .profileImageUrl(blockedUserDto.getProfileImageUrl())
                        .build())
                .toList();
        
        return MyPageResponse.builder()
                .profileImageUrl(myPageDto.getProfileImageUrl())
                .nickname(myPageDto.getNickname())
                .name(myPageDto.getName())
                .introduction(myPageDto.getIntroduction())
                .birthDate(myPageDto.getBirthDate())
                .email(myPageDto.getEmail())
                .addressSido(myPageDto.getAddressSido())
                .addressGugun(myPageDto.getAddressGugun())
                .createdAt(myPageDto.getCreatedAt())
                .favoriteProducts(myPageDto.getFavoriteProducts())
                .myProducts(myPageDto.getMyProducts())
                .purchaseRequests(myPageDto.getPurchaseRequests())
                .blockedUsers(blockedUserResponses)
                .build();
    }
}

