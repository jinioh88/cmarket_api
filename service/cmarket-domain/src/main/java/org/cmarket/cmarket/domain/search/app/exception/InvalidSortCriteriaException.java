package org.cmarket.cmarket.domain.search.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 잘못된 정렬 기준 예외
 * 
 * 정렬 기준이 유효하지 않을 때 발생합니다.
 */
public class InvalidSortCriteriaException extends BusinessException {
    
    public InvalidSortCriteriaException() {
        super(ErrorCode.INVALID_SORT_CRITERIA);
    }
    
    public InvalidSortCriteriaException(String message) {
        super(ErrorCode.INVALID_SORT_CRITERIA, message);
    }
}

