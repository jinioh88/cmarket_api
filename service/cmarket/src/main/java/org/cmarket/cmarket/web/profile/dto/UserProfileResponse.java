package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 유저 프로필 정보 웹 DTO
 * 
 * 웹 계층에서 사용하는 유저 프로필 정보 DTO입니다.
 */
@Getter
@Builder
public class UserProfileResponse {
    private String profileImageUrl;
    private String addressSido;
    private String addressGugun;
    private String nickname;
    private LocalDateTime createdAt;
    private String introduction;
    
    // todo: 등록한 상품 목록 (향후 Product 도메인에서 구현 예정)
    private List<Object> products;  // 빈 리스트로 반환
    
    /**
     * 앱 DTO에서 웹 DTO로 변환
     * 
     * @param userProfileDto 앱 DTO
     * @return 웹 DTO
     */
    public static UserProfileResponse fromDto(org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto userProfileDto) {
        return UserProfileResponse.builder()
                .profileImageUrl(userProfileDto.getProfileImageUrl())
                .addressSido(userProfileDto.getAddressSido())
                .addressGugun(userProfileDto.getAddressGugun())
                .nickname(userProfileDto.getNickname())
                .createdAt(userProfileDto.getCreatedAt())
                .introduction(userProfileDto.getIntroduction())
                .products(userProfileDto.getProducts())
                .build();
    }
}

