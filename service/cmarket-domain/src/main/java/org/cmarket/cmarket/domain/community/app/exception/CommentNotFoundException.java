package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 댓글을 찾을 수 없을 때 발생하는 예외
 */
public class CommentNotFoundException extends BusinessException {
    
    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
    
    public CommentNotFoundException(String message) {
        super(ErrorCode.COMMENT_NOT_FOUND, message);
    }
}

