package org.cmarket.cmarket.domain.map.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

public class InvalidLocationRangeException extends BusinessException {

    public InvalidLocationRangeException() {
        super(ErrorCode.INVALID_LOCATION_RANGE);
    }

    public InvalidLocationRangeException(String message) {
        super(ErrorCode.INVALID_LOCATION_RANGE, message);
    }
}
