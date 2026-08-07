package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.UserRole;

import java.time.LocalDateTime;

/**
 * 남이 보는 프로필 응답 DTO — GET /api/profile/{userId}
 *
 * ⚠️ **개인정보 필드가 이 클래스에 아예 없다. 더하지 마라.**
 *    실명(name)·생년월일(birthDate)·이메일(email)은 남이 볼 이유가 없다.
 *
 *    예전에는 내 정보와 한 클래스(UserProfileResponse)를 같이 썼다. 클래스에는 필드가
 *    다 선언돼 있고 무엇을 담을지는 팩토리 메서드 안에서만 갈려서, **셋이 그대로 실려
 *    나갔다.** 화면에서 안 그려도 응답에는 들어 있어 개발자도구로 보였고, 로그인만 하면
 *    아무나 받아 갈 수 있었다. 프론트에서 가리는 것으로는 못 막는다.
 *
 *    필드를 더할 때 「남에게 보여도 되나」를 사람이 따지지 않게 하려고 클래스를 갈랐다.
 *    본인이 보는 값은 {@link MyProfileResponse} 로 간다.
 */
@Getter
@Builder
public class PublicProfileResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private UserRole userRole;
    private String addressSido;
    private String addressGugun;
    private String introduction;
    /** 가입일. 중고거래에서 신뢰 신호라 남에게도 보여준다 */
    private LocalDateTime createdAt;

    /** 보는 사람이 이 사람을 차단했는지 */
    private Boolean isBlocked;
    /** 보는 사람이 이 사람을 신고했는지 */
    private Boolean isReported;

    public static PublicProfileResponse from(
            org.cmarket.cmarket.domain.profile.app.dto.UserProfileDto userProfileDto,
            Boolean isBlocked
    ) {
        return PublicProfileResponse.builder()
                .id(userProfileDto.getId())
                .nickname(userProfileDto.getNickname())
                .profileImageUrl(userProfileDto.getProfileImageUrl())
                .userRole(userProfileDto.getUserRole())
                .addressSido(userProfileDto.getAddressSido())
                .addressGugun(userProfileDto.getAddressGugun())
                .introduction(userProfileDto.getIntroduction())
                .createdAt(userProfileDto.getCreatedAt())
                .isBlocked(isBlocked)
                .isReported(userProfileDto.getIsReported())
                .build();
    }
}
