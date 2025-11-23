package org.cmarket.cmarket.domain.auth.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 만료된 인증코드 예외
 * 
 * 인증코드의 유효 시간이 지났을 때 발생합니다.
 */
public class ExpiredVerificationCodeException extends BusinessException {
    
    public ExpiredVerificationCodeException() {
        super(ErrorCode.EXPIRED_VERIFICATION_CODE);
    }
    
    public ExpiredVerificationCodeException(String message) {
        super(ErrorCode.EXPIRED_VERIFICATION_CODE, message);
    }
}

