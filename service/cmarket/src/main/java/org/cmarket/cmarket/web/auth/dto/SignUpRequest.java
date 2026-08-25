package org.cmarket.cmarket.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 회원가입 요청 DTO
 * 
 * 회원가입 시 필요한 모든 정보를 받습니다.
 */
@Getter
@NoArgsConstructor
public class SignUpRequest {
    
    @NotNull(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotNull(message = "비밀번호는 필수입니다.")
    private String password;
    
    @NotNull(message = "이름은 필수입니다.")
    @Size(max = 10, message = "이름은 최대 10자까지 입력 가능합니다.")
    private String name;
    
    @NotNull(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 10, message = "닉네임은 1자 이상 10자 이하여야 합니다.")
    private String nickname;
    
    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;
    
    @Size(max = 50, message = "시/도는 최대 50자까지 입력 가능합니다.")
    private String addressSido;  // 시/도
    
    @Size(max = 50, message = "구/군은 최대 50자까지 입력 가능합니다.")
    private String addressGugun;  // 구/군

    // ── 약관 동의 (#1088) ────────────────────────────────────────────────
    //
    // ⚠️ **일부러 필수로 안 막는다.** 원래 @NotNull + @AssertTrue 를 붙였다가 뺐다.
    //
    //    서버가 이 값을 요구하면 **지금 깔려 있는 앱이 전부 가입에 실패한다.** 웹은
    //    배포하면 그 순간 모두가 새 화면을 쓰지만, 앱은 테스터 폰에 옛 빌드가 남고
    //    각자 업데이트해야 바뀐다. 비공개 테스트 중이라 **새로 참여하는 테스터가
    //    가입을 못 하게 된다** — 테스터를 모으는 것이 출시의 병목인데 그것을 막는다.
    //
    //    그래서 지금은 이렇게 한다.
    //
    //      값이 true 로 오면   AGREED 로 적는다 (새 화면을 쓰는 사람)
    //      값이 안 오면        PRE_TERMS 로 남는다 (옛 앱)
    //
    // ⚠️ **동의를 강제하는 것은 지금 화면 몫이다.** 웹·앱 네 경로 모두 자물쇠가 두 겹이다
    //    (단추 끄기 + 제출 직전 검사). 실제 사용자는 그 둘을 지나야 가입할 수 있다.
    //    법적으로 중요한 것도 「사용자가 동의 화면을 보고 체크했는가」이고 그것은 화면이 지킨다.
    //
    // ⚠️ **새 앱 빌드가 테스터에게 다 퍼지면 다시 조여라.** 그때 되돌릴 것은 이 두 줄이다.
    //      @NotNull(message = "이용약관 동의 여부는 필수입니다.")
    //      @AssertTrue(message = "이용약관에 동의해야 가입할 수 있습니다.")
    //    ⚠️ 그때도 @AssertTrue 하나로는 못 막는다 — 규정상 null 은 통과다
    //       (Jakarta Bean Validation: "Null elements are considered valid").
    //       **@NotNull 을 같이 붙여야** 값을 안 보내는 요청이 막힌다
    private Boolean termsAgreed;

    private Boolean privacyAgreed;
}

