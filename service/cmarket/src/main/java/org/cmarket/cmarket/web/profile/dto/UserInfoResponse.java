package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 * 
 * 웹 계층에서 사용하는 사용자 정보 DTO입니다.
 */
@Getter
@Builder
public class UserInfoResponse {
    private String profileImageUrl;
    private String nickname;
    private String name;
    private String introduction;
    private LocalDate birthDate;
    private String email;
    private String addressSido;
    private String addressGugun;
    private LocalDateTime createdAt;
    
    /**
     * 앱 DTO에서 웹 DTO로 변환
     * 
     * @param myPageDto 앱 DTO
     * @return 웹 DTO
     */
    public static UserInfoResponse fromDto(org.cmarket.cmarket.domain.profile.app.dto.MyPageDto myPageDto) {
        return UserInfoResponse.builder()
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

