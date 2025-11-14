package org.cmarket.cmarket.domain.app.dto;

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
}

