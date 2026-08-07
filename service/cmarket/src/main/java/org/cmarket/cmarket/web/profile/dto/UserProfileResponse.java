package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.cmarket.cmarket.domain.auth.model.UserRole;

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
    private UserRole userRole;
    private AuthProvider provider;
    private String addressSido;
    private String addressGugun;
    private String nickname;
    private LocalDateTime createdAt;
    private String introduction;
    private String name;
    private LocalDate birthDate;
    private String email;
    private Boolean isBlocked;
    private Boolean isReported;

    
    /**
     * 앱 DTO에서 웹 DTO로 변환 (다른 사용자 프로필)
     *
     * ⚠️ 실명(name)·생년월일(birthDate)·이메일(email)을 **담지 않는다.**
     *    남이 볼 프로필이라 화면에 그릴 일이 없는 값들인데, 예전에는 응답에 실려 나갔다.
     *    화면에서 안 그려도 응답에는 들어 있어 개발자도구로 그대로 보였다 —
     *    로그인만 하면 아무나 남의 실명·생년월일·이메일을 받아 갈 수 있었다.
     *    프론트에서 가리는 것으로는 막을 수 없어 응답 자체에서 뺐다.
     *
     * 본인 정보 조회(fromMyPageDto)는 셋을 그대로 담는다 — 프로필 수정 화면이 다 쓴다.
     *
     * @param userProfileDto 앱 DTO
     * @param isBlocked 차단 여부
     * @return 웹 DTO
     */
    public static UserProfileResponse fromDto(
            org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto userProfileDto,
            Boolean isBlocked
    ) {
        return UserProfileResponse.builder()
                .id(userProfileDto.getId())
                .profileImageUrl(userProfileDto.getProfileImageUrl())
                .userRole(userProfileDto.getUserRole())
                .addressSido(userProfileDto.getAddressSido())
                .addressGugun(userProfileDto.getAddressGugun())
                .nickname(userProfileDto.getNickname())
                .createdAt(userProfileDto.getCreatedAt())
                .introduction(userProfileDto.getIntroduction())
                .isBlocked(isBlocked)
                .isReported(userProfileDto.getIsReported())
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
                .userRole(myPageDto.getUserRole())
                .provider(myPageDto.getProvider())
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

