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
     * 어느 쪽에서 로그인을 시작했나. "app"이면 앱 스킴으로 돌려보낸다.
     *
     * ⚠️ 주소가 아니라 **깃발**이다. 주소를 받아 그대로 쓰면 남이 만든 주소로 토큰이 날아간다
     *    (오픈 리다이렉트). 실제 주소는 application.properties에 못 박혀 있다.
     */
    public static final String CLIENT_PARAM_COOKIE_NAME = "oauth2_client";

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

        // 앱에서 시작했으면 깃발을 남겨 둔다. 카카오·구글에 다녀오는 동안 서버는
        // 아무것도 기억하지 않으므로(STATELESS) 쿠키에 맡긴다.
        String client = request.getParameter("client");
        if (StringUtils.hasText(client)) {
            CookieUtils.addCookie(
                    response,
                    CLIENT_PARAM_COOKIE_NAME,
                    client,
                    COOKIE_EXPIRE_SECONDS
            );
        } else {
            // ⚠️ 깃발이 없으면 **지운다.** 남겨 두면 앞선 앱 로그인의 깃발이 3분간 살아 있다가
            //    그 사이에 같은 브라우저로 웹 로그인을 하는 사람을 앱 스킴으로 보내 버린다.
            //    앱은 크롬 탭을 쓰므로 쿠키를 공유한다 — 실제로 일어날 수 있는 일이다.
            //    (앱에서 브라우저를 그냥 닫으면 서버는 아무 소식도 못 들어 쿠키만 남는다)
            CookieUtils.deleteCookie(request, response, CLIENT_PARAM_COOKIE_NAME);
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
        // ⚠️ 이 쿠키는 성공·실패 핸들러가 **읽은 뒤에** 지워야 한다. 먼저 지우면 늘 웹으로 간다.
        CookieUtils.deleteCookie(request, response, CLIENT_PARAM_COOKIE_NAME);
    }
}
