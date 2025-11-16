package org.cmarket.cmarket.domain.profile.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 마이페이지 정보 DTO
 * 
 * 앱 서비스에서 사용하는 마이페이지 정보 DTO입니다.
 */
@Getter
@Builder
public class MyPageDto {
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
    private List<BlockedUserDto> blockedUsers;
}

