package org.cmarket.cmarket.domain.product.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 상품 접근 권한 없음 예외
 * 
 * 본인 상품만 수정/삭제 가능할 때 발생하는 예외입니다.
 */
public class ProductAccessDeniedException extends BusinessException {
    
    public ProductAccessDeniedException() {
        super(ErrorCode.PRODUCT_ACCESS_DENIED);
    }
    
    public ProductAccessDeniedException(String message) {
        super(ErrorCode.PRODUCT_ACCESS_DENIED, message);
    }
}

