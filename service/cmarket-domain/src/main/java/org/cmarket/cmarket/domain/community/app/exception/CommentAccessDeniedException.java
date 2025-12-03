package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 댓글 접근 권한 없음 예외
 * 
 * 본인 댓글만 삭제 가능할 때 발생하는 예외입니다.
 */
public class CommentAccessDeniedException extends BusinessException {
    
    public CommentAccessDeniedException() {
        super(ErrorCode.COMMENT_ACCESS_DENIED);
    }
    
    public CommentAccessDeniedException(String message) {
        super(ErrorCode.COMMENT_ACCESS_DENIED, message);
    }
}

