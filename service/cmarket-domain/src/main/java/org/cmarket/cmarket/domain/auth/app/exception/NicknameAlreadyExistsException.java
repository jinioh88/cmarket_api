package org.cmarket.cmarket.domain.auth.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 닉네임 중복 예외
 * 
 * 이미 사용 중인 닉네임으로 회원가입을 시도할 때 발생합니다.
 */
public class NicknameAlreadyExistsException extends BusinessException {
    
    public NicknameAlreadyExistsException() {
        super(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }
    
    public NicknameAlreadyExistsException(String message) {
        super(ErrorCode.NICKNAME_ALREADY_EXISTS, message);
    }
}

