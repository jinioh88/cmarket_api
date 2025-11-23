package org.cmarket.cmarket.domain.auth.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 인증 실패 예외
 * 
 * 로그인 시 이메일 또는 비밀번호가 일치하지 않을 때 발생합니다.
 */
public class AuthenticationFailedException extends BusinessException {
    
    public AuthenticationFailedException() {
        super(ErrorCode.AUTHENTICATION_FAILED);
    }
    
    public AuthenticationFailedException(String message) {
        super(ErrorCode.AUTHENTICATION_FAILED, message);
    }
}

