package org.cmarket.cmarket.web.dto;

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
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 10, message = "이름은 최대 10자까지 입력 가능합니다.")
    private String name;
    
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 10, message = "닉네임은 최대 10자까지 입력 가능합니다.")
    private String nickname;
    
    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;
    
    @Size(max = 50, message = "시/도는 최대 50자까지 입력 가능합니다.")
    private String addressSido;  // 시/도
    
    @Size(max = 50, message = "구/군은 최대 50자까지 입력 가능합니다.")
    private String addressGugun;  // 구/군
}

