package org.cmarket.cmarket.domain.auth.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 이메일 중복 예외
 * 
 * 이미 등록된 이메일로 회원가입을 시도할 때 발생합니다.
 */
public class EmailAlreadyExistsException extends BusinessException {
    
    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    
    public EmailAlreadyExistsException(String message) {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, message);
    }
}

