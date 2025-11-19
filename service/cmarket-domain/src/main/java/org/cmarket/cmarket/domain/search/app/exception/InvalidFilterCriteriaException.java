package org.cmarket.cmarket.domain.search.app.exception;

/**
 * 잘못된 필터 조건 예외
 * 
 * 필터 조건이 유효하지 않을 때 발생합니다.
 */
public class InvalidFilterCriteriaException extends RuntimeException {
    
    public InvalidFilterCriteriaException(String message) {
        super(message);
    }
    
    public InvalidFilterCriteriaException(String message, Throwable cause) {
        super(message, cause);
    }
}

