package org.cmarket.cmarket.domain.auth.app.exception;

/**
 * 인증 실패 예외
 * 
 * 로그인 시 이메일 또는 비밀번호가 일치하지 않을 때 발생합니다.
 */
public class AuthenticationFailedException extends RuntimeException {
    
    public AuthenticationFailedException(String message) {
        super(message);
    }
    
    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

