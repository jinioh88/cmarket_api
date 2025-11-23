package org.cmarket.cmarket.web.common.response;

import lombok.Getter;
import org.cmarket.cmarket.domain.exception.ErrorCode;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final ResponseCode code;
    private final String message;
    private final String traceId;
    private final LocalDateTime timestamp;
    
    public ErrorResponse(ResponseCode code, String message, String traceId) {
        this.code = code;
        this.message = message;
        this.traceId = traceId;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(ErrorCode errorCode, String traceId) {
        this.code = convertToResponseCode(errorCode);
        this.message = errorCode.getMessage();
        this.traceId = traceId;
        this.timestamp = LocalDateTime.now();
    }
    
    private ResponseCode convertToResponseCode(ErrorCode errorCode) {
        int statusCode = errorCode.getStatusCode();
        for (ResponseCode responseCode : ResponseCode.values()) {
            if (responseCode.getCode() == statusCode) {
                return responseCode;
            }
        }
        return ResponseCode.INTERNAL_SERVER_ERROR;
    }
}

