package org.cmarket.cmarket.domain.community.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 행사 소식 글쓰기 거절 예외
 * 
 * 행사 소식(EVENT)은 수집 주소로만 만든다. 일반 글쓰기·수정으로 만들거나 옮기면 발생합니다.
 */
public class EventBoardWriteNotAllowedException extends BusinessException {
    
    public EventBoardWriteNotAllowedException() {
        super(ErrorCode.EVENT_BOARD_WRITE_NOT_ALLOWED);
    }
    
    public EventBoardWriteNotAllowedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
