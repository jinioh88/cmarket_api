package org.cmarket.cmarket.web.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 프로필 수정 요청 DTO
 * 
 * 웹 계층에서 사용하는 프로필 수정 요청 DTO입니다.
 */
@Getter
public class ProfileUpdateRequest {
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다.")
    private String nickname;
    
    private LocalDate birthDate;
    
    private String addressSido;
    
    private String addressGugun;
    
    private String profileImageUrl;
    
    @Size(max = 200, message = "소개글은 최대 200자까지 입력 가능합니다.")
    private String introduction;

    // ── 약관 동의 (#1088) — 소셜 가입 마무리에서만 온다 ──────────────────
    //
    // ⚠️ **여기엔 @NotNull·@AssertTrue 를 붙이면 안 된다.** 소셜은 별도 가입 API 가 없어
    //    가입 마무리도 프로필 수정도 **같은 PATCH /profile/me** 를 쓴다. 필수로 만들면
    //    프로필을 고칠 때마다 동의를 다시 보내야 한다.
    //
    // ⚠️ 그래서 **서버가 검증을 못 한다.** 소셜 가입에서 동의를 막는 것은 화면 몫이다
    //    (단추 끄기 + 제출 직전 검사, 두 겹). 이 칸은 「받았다는 사실을 적는」 용도다.
    private Boolean termsAgreed;

    private Boolean privacyAgreed;
}

