package org.cmarket.cmarket.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Google ID Token 로그인 요청 DTO
 * 
 * 프론트엔드에서 Google Sign-In SDK를 통해 받은 ID Token을 전달합니다.
 */
@Getter
@NoArgsConstructor
public class GoogleLoginRequest {
    
    /**
     * Google에서 발급한 ID Token
     * 
     * 프론트엔드에서 @react-oauth/google 등을 통해 받은 credential 값입니다.
     */
    @NotBlank(message = "ID Token은 필수입니다.")
    private String idToken;
    
    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }
}
