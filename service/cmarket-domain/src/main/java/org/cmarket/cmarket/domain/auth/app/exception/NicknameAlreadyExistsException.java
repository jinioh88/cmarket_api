package org.cmarket.cmarket.domain.auth.app.exception;

/**
 * 닉네임 중복 예외
 * 
 * 이미 사용 중인 닉네임으로 회원가입을 시도할 때 발생합니다.
 */
public class NicknameAlreadyExistsException extends RuntimeException {
    
    public NicknameAlreadyExistsException(String message) {
        super(message);
    }
    
    public NicknameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

