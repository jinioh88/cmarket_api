package org.cmarket.cmarket.web.exception;

import org.cmarket.cmarket.web.response.ErrorResponse;
import org.cmarket.cmarket.web.response.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID_KEY = "traceId";
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String traceId = getTraceId();
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.error("[{}] Validation error: {}", traceId, message, e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                message.isEmpty() ? "입력값 검증 실패" : message,
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        String traceId = getTraceId();
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.error("[{}] Bind error: {}", traceId, message, e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                message.isEmpty() ? "입력값 검증 실패" : message,
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Illegal argument: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        String traceId = getTraceId();
        
        log.error("[{}] Unexpected error occurred", traceId, e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다.",
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    private String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return traceId != null ? traceId : "unknown";
    }
}

