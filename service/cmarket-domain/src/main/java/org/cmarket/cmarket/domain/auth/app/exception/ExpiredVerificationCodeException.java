package org.cmarket.cmarket.domain.auth.app.exception;

/**
 * 만료된 인증코드 예외
 * 
 * 인증코드의 유효 시간이 지났을 때 발생합니다.
 */
public class ExpiredVerificationCodeException extends RuntimeException {
    
    public ExpiredVerificationCodeException(String message) {
        super(message);
    }
    
    public ExpiredVerificationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}

