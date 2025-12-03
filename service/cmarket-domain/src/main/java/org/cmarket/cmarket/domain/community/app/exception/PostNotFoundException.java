package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외
 */
public class PostNotFoundException extends BusinessException {
    
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
    
    public PostNotFoundException(String message) {
        super(ErrorCode.POST_NOT_FOUND, message);
    }
}

