package org.cmarket.cmarket.domain.product.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 찜 정보 없음 예외
 * 
 * 존재하지 않는 찜 정보에 접근할 때 발생하는 예외입니다.
 */
public class FavoriteNotFoundException extends BusinessException {
    
    public FavoriteNotFoundException() {
        super(ErrorCode.FAVORITE_NOT_FOUND);
    }
    
    public FavoriteNotFoundException(String message) {
        super(ErrorCode.FAVORITE_NOT_FOUND, message);
    }
}

