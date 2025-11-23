package org.cmarket.cmarket.domain.product.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 잘못된 상품 타입 예외
 * 
 * 상품 타입이 요청한 작업과 맞지 않을 때 발생하는 예외입니다.
 */
public class InvalidProductTypeException extends BusinessException {
    
    public InvalidProductTypeException() {
        super(ErrorCode.INVALID_PRODUCT_TYPE);
    }
    
    public InvalidProductTypeException(String message) {
        super(ErrorCode.INVALID_PRODUCT_TYPE, message);
    }
}

