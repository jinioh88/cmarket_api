package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 이미지 개수 초과 예외
 * 
 * 최대 5장까지 업로드 가능할 때 발생하는 예외입니다.
 */
public class InvalidImageCountException extends BusinessException {
    
    public InvalidImageCountException() {
        super(ErrorCode.INVALID_IMAGE_COUNT);
    }
    
    public InvalidImageCountException(String message) {
        super(ErrorCode.INVALID_IMAGE_COUNT, message);
    }
}

