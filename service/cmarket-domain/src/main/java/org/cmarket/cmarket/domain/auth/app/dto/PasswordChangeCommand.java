package org.cmarket.cmarket.domain.auth.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 비밀번호 변경 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class PasswordChangeCommand {
    private String email;
    private String currentPassword;
    private String newPassword;
}

