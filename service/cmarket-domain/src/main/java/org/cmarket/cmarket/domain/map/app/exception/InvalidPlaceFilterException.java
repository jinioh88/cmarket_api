package org.cmarket.cmarket.domain.map.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

public class InvalidPlaceFilterException extends BusinessException {

    public InvalidPlaceFilterException() {
        super(ErrorCode.INVALID_PLACE_FILTER);
    }

    public InvalidPlaceFilterException(String message) {
        super(ErrorCode.INVALID_PLACE_FILTER, message);
    }
}
