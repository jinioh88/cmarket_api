package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 유저 프로필 정보 웹 DTO
 * 
 * 웹 계층에서 사용하는 유저 프로필 정보 DTO입니다.
 */
@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String profileImageUrl;
    private String addressSido;
    private String addressGugun;
    private String nickname;
    private LocalDateTime createdAt;
    private String introduction;
    private String name;
    private LocalDate birthDate;
    private String email;

    
    /**
     * 앱 DTO에서 웹 DTO로 변환 (다른 사용자 프로필)
     * 
     * @param userProfileDto 앱 DTO
     * @return 웹 DTO
     */
    public static UserProfileResponse fromDto(org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto userProfileDto) {
        return UserProfileResponse.builder()
                .id(userProfileDto.getId())
                .profileImageUrl(userProfileDto.getProfileImageUrl())
                .addressSido(userProfileDto.getAddressSido())
                .addressGugun(userProfileDto.getAddressGugun())
                .nickname(userProfileDto.getNickname())
                .createdAt(userProfileDto.getCreatedAt())
                .introduction(userProfileDto.getIntroduction())
                .name(userProfileDto.getName())
                .birthDate(userProfileDto.getBirthDate())
                .email(userProfileDto.getEmail())
                .build();
    }
    
    /**
     * 앱 DTO에서 웹 DTO로 변환 (본인 정보)
     * 
     * @param myPageDto 앱 DTO
     * @return 웹 DTO
     */
    public static UserProfileResponse fromMyPageDto(org.cmarket.cmarket.domain.profile.app.dto.MyPageDto myPageDto) {
        return UserProfileResponse.builder()
                .id(myPageDto.getId())
                .profileImageUrl(myPageDto.getProfileImageUrl())
                .nickname(myPageDto.getNickname())
                .name(myPageDto.getName())
                .introduction(myPageDto.getIntroduction())
                .birthDate(myPageDto.getBirthDate())
                .email(myPageDto.getEmail())
                .addressSido(myPageDto.getAddressSido())
                .addressGugun(myPageDto.getAddressGugun())
                .createdAt(myPageDto.getCreatedAt())
                .build();
    }
}

