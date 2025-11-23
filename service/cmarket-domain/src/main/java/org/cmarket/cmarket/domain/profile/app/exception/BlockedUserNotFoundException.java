package org.cmarket.cmarket.domain.profile.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 차단한 유저를 찾을 수 없음 예외
 * 
 * 요청한 차단 관계가 존재하지 않을 때 발생합니다.
 */
public class BlockedUserNotFoundException extends BusinessException {
    
    public BlockedUserNotFoundException() {
        super(ErrorCode.BLOCKED_USER_NOT_FOUND);
    }
    
    public BlockedUserNotFoundException(String message) {
        super(ErrorCode.BLOCKED_USER_NOT_FOUND, message);
    }
}

