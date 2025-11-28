package org.cmarket.cmarket.web.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Token 갱신 응답 DTO
 * 
 * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받을 때 사용합니다.
 */
@Getter
@Builder
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
}

