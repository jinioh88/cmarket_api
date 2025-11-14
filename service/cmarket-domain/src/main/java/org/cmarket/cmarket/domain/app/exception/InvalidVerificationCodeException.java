package org.cmarket.cmarket.domain.app.exception;

/**
 * 잘못된 인증코드 예외
 * 
 * 입력한 인증코드가 일치하지 않을 때 발생합니다.
 */
public class InvalidVerificationCodeException extends RuntimeException {
    
    public InvalidVerificationCodeException(String message) {
        super(message);
    }
    
    public InvalidVerificationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}

