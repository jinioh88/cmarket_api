package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.cmarket.cmarket.domain.auth.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 내 정보 응답 DTO — GET /api/profile/me
 *
 * ⚠️ **여기에는 개인정보를 담아도 된다.** 본인이 본인 것을 보는 응답이다.
 *    실명·생년월일·이메일은 프로필 수정 화면이 다 쓴다.
 *
 * ⚠️ **남이 볼 프로필은 {@link PublicProfileResponse} 다. 절대 이 클래스를 쓰지 마라.**
 *    예전에는 둘이 한 클래스(UserProfileResponse)였고, 무엇을 담을지는 팩토리 메서드
 *    안에서만 갈렸다. 그래서 남의 프로필 응답에 실명·생년월일·이메일이 실려 나갔다 —
 *    화면에 안 그릴 뿐 응답에는 있어서 로그인만 하면 아무나 받아 갈 수 있었다.
 *    필드를 더할 때 「이건 남에게도 보여도 되나」를 사람이 매번 따져야 했기 때문이다.
 *    이제는 **클래스가 갈라져 있어 그 판단이 강제된다.**
 */
@Getter
@Builder
public class MyProfileResponse {
    private Long id;
    private String nickname;
    private String profileImageUrl;
    private UserRole userRole;
    private String addressSido;
    private String addressGugun;
    private String introduction;
    private LocalDateTime createdAt;

    // ↓ 여기서부터는 **본인만 볼 값**이다. PublicProfileResponse 에는 없다.
    /** 어떻게 가입했나. 비밀번호 바꾸는 자리를 그릴지 가른다 */
    private AuthProvider provider;
    private String name;
    private LocalDate birthDate;
    private String email;

    public static MyProfileResponse from(org.cmarket.cmarket.domain.profile.app.dto.MyPageDto myPageDto) {
        return MyProfileResponse.builder()
                .id(myPageDto.getId())
                .nickname(myPageDto.getNickname())
                .profileImageUrl(myPageDto.getProfileImageUrl())
                .userRole(myPageDto.getUserRole())
                .addressSido(myPageDto.getAddressSido())
                .addressGugun(myPageDto.getAddressGugun())
                .introduction(myPageDto.getIntroduction())
                .createdAt(myPageDto.getCreatedAt())
                .provider(myPageDto.getProvider())
                .name(myPageDto.getName())
                .birthDate(myPageDto.getBirthDate())
                .email(myPageDto.getEmail())
                .build();
    }
}
