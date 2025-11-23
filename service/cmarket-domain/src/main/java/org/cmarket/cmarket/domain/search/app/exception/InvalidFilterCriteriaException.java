package org.cmarket.cmarket.domain.search.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 잘못된 필터 조건 예외
 * 
 * 필터 조건이 유효하지 않을 때 발생합니다.
 */
public class InvalidFilterCriteriaException extends BusinessException {
    
    public InvalidFilterCriteriaException() {
        super(ErrorCode.INVALID_FILTER_CRITERIA);
    }
    
    public InvalidFilterCriteriaException(String message) {
        super(ErrorCode.INVALID_FILTER_CRITERIA, message);
    }
}

