package org.cmarket.cmarket.web.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
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
}

