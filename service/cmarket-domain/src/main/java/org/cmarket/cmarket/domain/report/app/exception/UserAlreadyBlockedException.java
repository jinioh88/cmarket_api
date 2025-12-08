package org.cmarket.cmarket.domain.report.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 이미 차단된 사용자에 대한 중복 차단 시 발생하는 예외.
 */
public class UserAlreadyBlockedException extends BusinessException {

    public UserAlreadyBlockedException() {
        super(ErrorCode.USER_ALREADY_BLOCKED);
    }

    public UserAlreadyBlockedException(String message) {
        super(ErrorCode.USER_ALREADY_BLOCKED, message);
    }
}

