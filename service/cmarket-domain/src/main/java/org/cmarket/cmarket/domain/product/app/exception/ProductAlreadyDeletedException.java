package org.cmarket.cmarket.domain.product.app.exception;

/**
 * 이미 삭제된 상품 예외
 * 
 * 이미 삭제된 상품에 대한 작업을 시도할 때 발생하는 예외입니다.
 */
public class ProductAlreadyDeletedException extends RuntimeException {
    
    public ProductAlreadyDeletedException(String message) {
        super(message);
    }
}

