package org.cmarket.cmarket.domain.product.app.exception;

/**
 * 상품 접근 권한 없음 예외
 * 
 * 본인 상품만 수정/삭제 가능할 때 발생하는 예외입니다.
 */
public class ProductAccessDeniedException extends RuntimeException {
    
    public ProductAccessDeniedException(String message) {
        super(message);
    }
}

