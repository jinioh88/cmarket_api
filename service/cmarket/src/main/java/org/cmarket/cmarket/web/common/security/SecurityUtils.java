package org.cmarket.cmarket.web.common.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 인증된 사용자 정보를 추출하는 유틸리티 클래스
 * 
 * Spring Security의 SecurityContextHolder에서 현재 로그인한 사용자 정보를 추출합니다.
 * 
 * 주요 기능:
 * - 현재 로그인한 사용자의 이메일 추출
 * - 현재 로그인한 사용자의 Authentication 객체 반환
 * 
 * 참고: JWT 토큰 기반 인증에서는 principal이 email(String)로 설정됩니다.
 */
public final class SecurityUtils {
    
    /**
     * 인스턴스화 방지
     */
    private SecurityUtils() {
        throw new UnsupportedOperationException("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
    
    /**
     * 현재 로그인한 사용자의 이메일 추출
     * 
     * @return 사용자 이메일
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = getCurrentAuthentication();
        return (String) authentication.getPrincipal();
    }
    
    /**
     * 현재 로그인한 사용자의 Authentication 객체 반환
     * 
     * @return Authentication 객체
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Authentication getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!isAuthenticatedInternal(authentication)) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        
        return authentication;
    }
    
    /**
     * 현재 로그인한 사용자가 인증되었는지 확인
     * 
     * @return 인증되었으면 true, 그렇지 않으면 false
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthenticatedInternal(authentication);
    }
    
    private static boolean isAuthenticatedInternal(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}

