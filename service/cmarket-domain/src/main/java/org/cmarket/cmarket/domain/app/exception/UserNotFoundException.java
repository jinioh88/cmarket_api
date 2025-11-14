package org.cmarket.cmarket.domain.app.exception;

/**
 * 사용자를 찾을 수 없음 예외
 * 
 * 요청한 사용자가 존재하지 않을 때 발생합니다.
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

