package org.cmarket.cmarket.web.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SuccessResponse<T> {
    private final ResponseCode code;
    private final String message;
    private final T data;
    
    public SuccessResponse(ResponseCode code, T data) {
        this.code = code;
        this.message = code.getMessage();
        this.data = data;
    }
}

