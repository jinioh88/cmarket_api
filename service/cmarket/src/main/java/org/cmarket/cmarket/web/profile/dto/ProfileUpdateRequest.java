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
    
    @Size(max = 1000, message = "소개글은 최대 1000자까지 입력 가능합니다.")
    private String introduction;
}

