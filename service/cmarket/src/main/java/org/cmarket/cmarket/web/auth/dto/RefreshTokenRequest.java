package org.cmarket.cmarket.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Refresh Token 요청 DTO
 * 
 * Access Token 갱신을 위해 Refresh Token을 받습니다.
 */
@Getter
@NoArgsConstructor
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}

