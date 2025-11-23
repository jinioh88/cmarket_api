package org.cmarket.cmarket.domain.auth.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 사용자를 찾을 수 없음 예외
 * 
 * 요청한 사용자가 존재하지 않을 때 발생합니다.
 */
public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
    
    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }
}

