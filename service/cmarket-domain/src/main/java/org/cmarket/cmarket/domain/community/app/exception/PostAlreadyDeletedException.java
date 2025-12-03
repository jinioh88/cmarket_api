package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 이미 삭제된 게시글 예외
 */
public class PostAlreadyDeletedException extends BusinessException {
    
    public PostAlreadyDeletedException() {
        super(ErrorCode.POST_ALREADY_DELETED);
    }
    
    public PostAlreadyDeletedException(String message) {
        super(ErrorCode.POST_ALREADY_DELETED, message);
    }
}

