package org.cmarket.cmarket.domain.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 인증코드 검증 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class EmailVerificationVerifyCommand {
    private String email;
    private String verificationCode;
}

