package org.cmarket.cmarket.domain.app.exception;

/**
 * 이메일 중복 예외
 * 
 * 이미 등록된 이메일로 회원가입을 시도할 때 발생합니다.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
    
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

