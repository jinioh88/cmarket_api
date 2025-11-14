package org.cmarket.cmarket.web.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 응답 DTO
 * 
 * 웹 계층에서 사용하는 로그인 응답 DTO입니다.
 * Access Token과 Refresh Token을 포함합니다.
 */
@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserWebDto user;
}

