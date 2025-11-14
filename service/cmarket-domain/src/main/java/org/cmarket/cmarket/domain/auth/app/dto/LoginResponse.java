package org.cmarket.cmarket.domain.auth.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 응답 DTO
 * 
 * 앱 서비스에서 사용하는 로그인 응답 DTO입니다.
 * 토큰은 컨트롤러에서 추가됩니다.
 */
@Getter
@Builder
public class LoginResponse {
    private UserDto user;
}

