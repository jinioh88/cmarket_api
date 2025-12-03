package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 게시글 접근 권한 없음 예외
 * 
 * 본인 게시글만 수정/삭제 가능할 때 발생하는 예외입니다.
 */
public class PostAccessDeniedException extends BusinessException {
    
    public PostAccessDeniedException() {
        super(ErrorCode.POST_ACCESS_DENIED);
    }
    
    public PostAccessDeniedException(String message) {
        super(ErrorCode.POST_ACCESS_DENIED, message);
    }
}

