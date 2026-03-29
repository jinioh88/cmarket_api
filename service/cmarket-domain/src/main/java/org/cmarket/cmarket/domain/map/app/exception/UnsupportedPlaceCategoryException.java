package org.cmarket.cmarket.domain.map.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

public class UnsupportedPlaceCategoryException extends BusinessException {

    public UnsupportedPlaceCategoryException() {
        super(ErrorCode.UNSUPPORTED_PLACE_CATEGORY);
    }

    public UnsupportedPlaceCategoryException(String message) {
        super(ErrorCode.UNSUPPORTED_PLACE_CATEGORY, message);
    }
}
