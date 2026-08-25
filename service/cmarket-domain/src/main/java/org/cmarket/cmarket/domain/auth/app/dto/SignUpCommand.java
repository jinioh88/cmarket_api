package org.cmarket.cmarket.domain.auth.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 회원가입 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class SignUpCommand {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
    // 약관 동의 (#1088). 옛 앱은 안 보내므로 null 로 온다 — 그때는 AGREED 로 적으면 안 된다
    private Boolean termsAgreed;
    private Boolean privacyAgreed;
}

