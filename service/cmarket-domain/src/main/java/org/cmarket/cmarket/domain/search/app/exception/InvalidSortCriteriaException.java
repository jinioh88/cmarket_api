package org.cmarket.cmarket.domain.search.app.exception;

/**
 * 잘못된 정렬 기준 예외
 * 
 * 정렬 기준이 유효하지 않을 때 발생합니다.
 */
public class InvalidSortCriteriaException extends RuntimeException {
    
    public InvalidSortCriteriaException(String message) {
        super(message);
    }
    
    public InvalidSortCriteriaException(String message, Throwable cause) {
        super(message, cause);
    }
}

