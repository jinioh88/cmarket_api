package org.cmarket.cmarket.web.common.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.web.common.response.ErrorResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
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
    
    /**
     * 비즈니스 예외 공통 처리
     * 
     * 모든 BusinessException을 상속받은 예외는 이 핸들러에서 처리됩니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Business exception: {} - {}", traceId, e.getClass().getSimpleName(), e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode(), traceId);
        
        return ResponseEntity.status(e.getErrorCode().getStatusCode()).body(errorResponse);
    }
    
    /**
     * Spring Validation 예외 처리 (@Valid 실패)
     */
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
    
    /**
     * Spring Bind 예외 처리 (@ModelAttribute 검증 실패)
     */
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
    
    /**
     * 잘못된 인자 예외 처리
     */
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
    
    /**
     * 예상치 못한 예외 처리 (최후의 안전망)
     */
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

