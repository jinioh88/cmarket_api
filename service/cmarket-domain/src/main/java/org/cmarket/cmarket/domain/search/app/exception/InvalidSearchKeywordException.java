package org.cmarket.cmarket.domain.search.app.exception;

/**
 * 잘못된 검색어 형식 예외
 * 
 * 검색어가 유효하지 않을 때 발생합니다.
 */
public class InvalidSearchKeywordException extends RuntimeException {
    
    public InvalidSearchKeywordException(String message) {
        super(message);
    }
    
    public InvalidSearchKeywordException(String message, Throwable cause) {
        super(message, cause);
    }
}

