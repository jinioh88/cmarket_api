package org.cmarket.cmarket.domain.map.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

public class PlaceNotFoundException extends BusinessException {

    public PlaceNotFoundException() {
        super(ErrorCode.PLACE_NOT_FOUND);
    }

    public PlaceNotFoundException(String message) {
        super(ErrorCode.PLACE_NOT_FOUND, message);
    }
}
