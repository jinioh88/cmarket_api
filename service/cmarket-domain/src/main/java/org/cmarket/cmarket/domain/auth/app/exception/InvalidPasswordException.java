package org.cmarket.cmarket.domain.auth.app.exception;

/**
 * 잘못된 비밀번호 예외
 * 
 * 비밀번호가 유효하지 않거나 비밀번호 검증에 실패할 때 발생합니다.
 */
public class InvalidPasswordException extends RuntimeException {
    
    public InvalidPasswordException(String message) {
        super(message);
    }
    
    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}

