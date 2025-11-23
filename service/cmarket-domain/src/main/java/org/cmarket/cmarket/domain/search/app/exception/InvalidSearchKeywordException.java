package org.cmarket.cmarket.domain.search.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 잘못된 검색어 형식 예외
 * 
 * 검색어가 유효하지 않을 때 발생합니다.
 */
public class InvalidSearchKeywordException extends BusinessException {
    
    public InvalidSearchKeywordException() {
        super(ErrorCode.INVALID_SEARCH_KEYWORD);
    }
    
    public InvalidSearchKeywordException(String message) {
        super(ErrorCode.INVALID_SEARCH_KEYWORD, message);
    }
}

