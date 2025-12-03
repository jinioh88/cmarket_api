package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 댓글 depth 초과 예외
 * 
 * 최대 3단계까지만 허용할 때 발생하는 예외입니다.
 */
public class CommentDepthExceededException extends BusinessException {
    
    public CommentDepthExceededException() {
        super(ErrorCode.COMMENT_DEPTH_EXCEEDED);
    }
    
    public CommentDepthExceededException(String message) {
        super(ErrorCode.COMMENT_DEPTH_EXCEEDED, message);
    }
}

