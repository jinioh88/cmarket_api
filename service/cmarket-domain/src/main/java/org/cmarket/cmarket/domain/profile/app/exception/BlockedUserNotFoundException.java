package org.cmarket.cmarket.domain.profile.app.exception;

/**
 * 차단한 유저를 찾을 수 없음 예외
 * 
 * 요청한 차단 관계가 존재하지 않을 때 발생합니다.
 */
public class BlockedUserNotFoundException extends RuntimeException {
    
    public BlockedUserNotFoundException(String message) {
        super(message);
    }
    
    public BlockedUserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

