package org.cmarket.cmarket.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 Enum
 * 
 * 모든 비즈니스 예외의 에러 코드, 메시지, HTTP 상태 코드를 관리합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 인증 관련 (400)
    INVALID_VERIFICATION_CODE(400, "인증코드가 일치하지 않습니다."),
    EXPIRED_VERIFICATION_CODE(400, "인증코드가 만료되었습니다."),
    INVALID_PASSWORD(400, "비밀번호가 유효하지 않습니다."),
    
    // 인증 관련 (401)
    AUTHENTICATION_FAILED(401, "이메일 또는 비밀번호가 일치하지 않습니다."),
    
    // 인증 관련 (409)
    EMAIL_ALREADY_EXISTS(409, "이미 등록된 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(409, "이미 사용 중인 닉네임입니다."),
    
    // 사용자 관련 (404)
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    BLOCKED_USER_NOT_FOUND(404, "차단된 사용자를 찾을 수 없습니다."),
    
    // 상품 관련 (400)
    PRODUCT_ALREADY_DELETED(400, "이미 삭제된 상품입니다."),
    INVALID_PRODUCT_TYPE(400, "잘못된 상품 타입입니다."),
    
    // 상품 관련 (403)
    PRODUCT_ACCESS_DENIED(403, "상품에 대한 접근 권한이 없습니다."),
    
    // 상품 관련 (404)
    PRODUCT_NOT_FOUND(404, "상품을 찾을 수 없습니다."),
    FAVORITE_NOT_FOUND(404, "찜 정보를 찾을 수 없습니다."),
    
    // 검색 관련 (400)
    INVALID_SEARCH_KEYWORD(400, "검색어가 유효하지 않습니다."),
    INVALID_SORT_CRITERIA(400, "정렬 기준이 유효하지 않습니다."),
    INVALID_FILTER_CRITERIA(400, "필터 기준이 유효하지 않습니다."),
    
    // 커뮤니티 관련 (400)
    POST_ALREADY_DELETED(400, "이미 삭제된 게시글입니다."),
    COMMENT_DEPTH_EXCEEDED(400, "댓글은 최대 3단계까지만 작성할 수 있습니다."),
    INVALID_IMAGE_COUNT(400, "이미지는 최대 5장까지 등록 가능합니다."),
    
    // 커뮤니티 관련 (403)
    POST_ACCESS_DENIED(403, "게시글에 대한 접근 권한이 없습니다."),
    COMMENT_ACCESS_DENIED(403, "댓글에 대한 접근 권한이 없습니다."),
    
    // 커뮤니티 관련 (404)
    POST_NOT_FOUND(404, "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(404, "댓글을 찾을 수 없습니다.");
    
    private final int statusCode;
    private final String message;
    
    public int getStatusCode() {
        return statusCode;
    }
}

