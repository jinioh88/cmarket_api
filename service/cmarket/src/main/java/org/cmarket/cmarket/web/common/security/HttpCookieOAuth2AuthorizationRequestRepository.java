package org.cmarket.cmarket.web.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OAuth2 인증 요청을 쿠키에 저장하는 Repository
 * 
 * JWT 기반 STATELESS 세션 정책에서 OAuth2를 사용하기 위해
 * 세션 대신 쿠키에 인증 요청 정보를 저장합니다.
 * 
 * OAuth2 인증 흐름:
 * 1. 사용자가 /oauth2/authorization/google 접근
 * 2. saveAuthorizationRequest() 호출 → 쿠키에 인증 요청 저장
 * 3. 구글 로그인 페이지로 리다이렉트
 * 4. 구글에서 콜백 /login/oauth2/code/google
 * 5. loadAuthorizationRequest() 호출 → 쿠키에서 인증 요청 로드
 * 6. removeAuthorizationRequest() 호출 → 쿠키 삭제
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository 
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    
    /**
     * OAuth2 인증 요청을 저장하는 쿠키 이름
     */
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    
    /**
     * 리다이렉트 URI를 저장하는 쿠키 이름 (프론트엔드에서 전달한 경우)
     */
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    
    /**
     * 쿠키 만료 시간 (3분 = 180초)
     * OAuth2 인증 과정이 이 시간 내에 완료되어야 함
     */
    private static final int COOKIE_EXPIRE_SECONDS = 180;
    
    /**
     * 쿠키에서 OAuth2 인증 요청을 로드합니다.
     *
     * @param request HTTP 요청
     * @return OAuth2AuthorizationRequest (없으면 null)
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }
    
    /**
     * OAuth2 인증 요청을 쿠키에 저장합니다.
     *
     * @param authorizationRequest 저장할 인증 요청
     * @param request HTTP 요청
     * @param response HTTP 응답
     */
    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            // 인증 요청이 null이면 기존 쿠키 삭제
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }
        
        // 인증 요청을 직렬화하여 쿠키에 저장
        CookieUtils.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtils.serialize(authorizationRequest),
                COOKIE_EXPIRE_SECONDS
        );
        
        // 프론트엔드에서 전달한 redirect_uri가 있으면 별도 쿠키에 저장
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            CookieUtils.addCookie(
                    response,
                    REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin,
                    COOKIE_EXPIRE_SECONDS
            );
        }
    }
    
    /**
     * OAuth2 인증 요청을 제거하고 반환합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 제거된 OAuth2AuthorizationRequest
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
        
        // 쿠키 삭제는 removeAuthorizationRequestCookies()에서 처리
        // (성공/실패 핸들러에서 호출)
        
        return authorizationRequest;
    }
    
    /**
     * OAuth2 인증 관련 쿠키를 모두 삭제합니다.
     * 인증 성공/실패 핸들러에서 호출해야 합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }
}
