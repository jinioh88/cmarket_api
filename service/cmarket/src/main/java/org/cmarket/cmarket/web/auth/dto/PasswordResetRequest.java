package org.cmarket.cmarket.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 요청 DTO
 * 
 * 클라이언트에서 이미 인증코드 검증을 완료한 후 호출됩니다.
 */
@Getter
@NoArgsConstructor
public class PasswordResetRequest {
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;
    
    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;
}

