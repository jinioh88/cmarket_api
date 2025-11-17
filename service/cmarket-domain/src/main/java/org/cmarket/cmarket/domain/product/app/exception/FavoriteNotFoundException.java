package org.cmarket.cmarket.domain.product.app.exception;

/**
 * 찜 정보 없음 예외
 * 
 * 존재하지 않는 찜 정보에 접근할 때 발생하는 예외입니다.
 */
public class FavoriteNotFoundException extends RuntimeException {
    
    public FavoriteNotFoundException(String message) {
        super(message);
    }
}

