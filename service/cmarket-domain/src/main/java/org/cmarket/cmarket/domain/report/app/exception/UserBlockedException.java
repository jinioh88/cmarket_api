package org.cmarket.cmarket.domain.report.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 차단된 사용자와의 상호작용 시도 시 발생하는 예외
 */
public class UserBlockedException extends BusinessException {
    
    public UserBlockedException() {
        super(ErrorCode.USER_BLOCKED);
    }
    
    public UserBlockedException(String message) {
        super(ErrorCode.USER_BLOCKED, message);
    }
}

