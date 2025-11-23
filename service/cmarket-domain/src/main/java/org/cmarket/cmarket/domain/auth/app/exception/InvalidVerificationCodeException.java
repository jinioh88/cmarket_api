package org.cmarket.cmarket.domain.auth.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 잘못된 인증코드 예외
 * 
 * 입력한 인증코드가 일치하지 않을 때 발생합니다.
 */
public class InvalidVerificationCodeException extends BusinessException {
    
    public InvalidVerificationCodeException() {
        super(ErrorCode.INVALID_VERIFICATION_CODE);
    }
    
    public InvalidVerificationCodeException(String message) {
        super(ErrorCode.INVALID_VERIFICATION_CODE, message);
    }
}

