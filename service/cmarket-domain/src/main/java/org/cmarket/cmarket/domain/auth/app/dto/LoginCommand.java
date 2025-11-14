package org.cmarket.cmarket.domain.auth.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class LoginCommand {
    private String email;
    private String password;
}

