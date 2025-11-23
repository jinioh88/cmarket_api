package org.cmarket.cmarket.domain.product.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 이미 삭제된 상품 예외
 * 
 * 이미 삭제된 상품에 대한 작업을 시도할 때 발생하는 예외입니다.
 */
public class ProductAlreadyDeletedException extends BusinessException {
    
    public ProductAlreadyDeletedException() {
        super(ErrorCode.PRODUCT_ALREADY_DELETED);
    }
    
    public ProductAlreadyDeletedException(String message) {
        super(ErrorCode.PRODUCT_ALREADY_DELETED, message);
    }
}

