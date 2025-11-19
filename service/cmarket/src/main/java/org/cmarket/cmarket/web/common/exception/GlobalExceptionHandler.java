package org.cmarket.cmarket.web.common.exception;

import org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException;
import org.cmarket.cmarket.domain.auth.app.exception.EmailAlreadyExistsException;
import org.cmarket.cmarket.domain.auth.app.exception.ExpiredVerificationCodeException;
import org.cmarket.cmarket.domain.auth.app.exception.InvalidPasswordException;
import org.cmarket.cmarket.domain.auth.app.exception.InvalidVerificationCodeException;
import org.cmarket.cmarket.domain.auth.app.exception.NicknameAlreadyExistsException;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
import org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException;
import org.cmarket.cmarket.domain.profile.app.exception.BlockedUserNotFoundException;
import org.cmarket.cmarket.domain.search.app.exception.InvalidSearchKeywordException;
import org.cmarket.cmarket.domain.search.app.exception.InvalidSortCriteriaException;
import org.cmarket.cmarket.domain.search.app.exception.InvalidFilterCriteriaException;
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
    
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Email already exists: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.CONFLICT,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    @ExceptionHandler(NicknameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleNicknameAlreadyExistsException(NicknameAlreadyExistsException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Nickname already exists: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.CONFLICT,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCodeException(InvalidVerificationCodeException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Invalid verification code: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(ExpiredVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleExpiredVerificationCodeException(ExpiredVerificationCodeException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Expired verification code: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Invalid password: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        String traceId = getTraceId();
        
        log.error("[{}] User not found: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.NOT_FOUND,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(AuthenticationFailedException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Authentication failed: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.UNAUTHORIZED,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(BlockedUserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBlockedUserNotFoundException(BlockedUserNotFoundException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Blocked user not found: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.NOT_FOUND,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Product not found: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.NOT_FOUND,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(org.cmarket.cmarket.domain.product.app.exception.ProductAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleProductAccessDeniedException(
            org.cmarket.cmarket.domain.product.app.exception.ProductAccessDeniedException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Product access denied: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.FORBIDDEN,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(org.cmarket.cmarket.domain.product.app.exception.ProductAlreadyDeletedException.class)
    public ResponseEntity<ErrorResponse> handleProductAlreadyDeletedException(
            org.cmarket.cmarket.domain.product.app.exception.ProductAlreadyDeletedException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Product already deleted: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(org.cmarket.cmarket.domain.product.app.exception.FavoriteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFavoriteNotFoundException(
            org.cmarket.cmarket.domain.product.app.exception.FavoriteNotFoundException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Favorite not found: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.NOT_FOUND,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(org.cmarket.cmarket.domain.product.app.exception.InvalidProductTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProductTypeException(
            org.cmarket.cmarket.domain.product.app.exception.InvalidProductTypeException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Invalid product type: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidSearchKeywordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSearchKeywordException(InvalidSearchKeywordException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Invalid search keyword: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidSortCriteriaException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortCriteriaException(InvalidSortCriteriaException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Invalid sort criteria: {}", traceId, e.getMessage(), e);
        
        ErrorResponse errorResponse = new ErrorResponse(
                ResponseCode.BAD_REQUEST,
                e.getMessage(),
                traceId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(InvalidFilterCriteriaException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFilterCriteriaException(InvalidFilterCriteriaException e) {
        String traceId = getTraceId();
        
        log.error("[{}] Invalid filter criteria: {}", traceId, e.getMessage(), e);
        
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

