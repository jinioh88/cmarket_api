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
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import jakarta.servlet.http.HttpServletRequest;
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
     * SSE 비동기 요청 타임아웃 예외 처리
     * 
     * SSE 연결의 정상적인 타임아웃이므로 에러로 처리하지 않습니다.
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Void> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e, HttpServletRequest request) {
        String traceId = getTraceId();
        String requestPath = request.getRequestURI();
        
        // SSE 엔드포인트의 타임아웃은 정상 동작이므로 DEBUG 레벨로만 로깅
        if (requestPath != null && requestPath.contains("/notifications/stream")) {
            log.debug("[{}] SSE connection timeout (normal): {}", traceId, requestPath);
        } else {
            log.warn("[{}] Async request timeout: {}", traceId, requestPath, e);
        }
        
        // 응답이 이미 커밋되었을 수 있으므로 null 반환
        return null;
    }
    
    /**
     * 예상치 못한 예외 처리 (최후의 안전망)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        String traceId = getTraceId();
        String requestPath = request.getRequestURI();
        
        // SSE 엔드포인트에서 발생한 예외는 별도 처리
        if (requestPath != null && requestPath.contains("/notifications/stream")) {
            // 응답이 이미 커밋되었거나 Content-Type이 text/event-stream인 경우
            // 에러 응답을 반환하지 않고 로그만 남김
            log.warn("[{}] SSE endpoint error (response may be committed): {}", traceId, e.getMessage());
            return null;
        }
        
        log.error("[{}] Unexpected error occurred", traceId, e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다.",
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 파일 업로드 용량 초과 예외 처리
     */
    @ExceptionHandler({
            MaxUploadSizeExceededException.class,
            MultipartException.class
    })
    public ResponseEntity<ErrorResponse> handleMultipartSizeExceeded(Exception e) {
        String traceId = getTraceId();
        
        log.error("[{}] File upload size exceeded: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.PAYLOAD_TOO_LARGE,
                "최대 5MB 이하의 이미지 파일만 업로드할 수 있습니다.",
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }
    
    private String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        return traceId != null ? traceId : "unknown";
    }
}

