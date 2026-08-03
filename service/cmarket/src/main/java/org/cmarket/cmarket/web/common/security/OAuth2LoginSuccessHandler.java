package org.cmarket.cmarket.web.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cmarket.cmarket.domain.auth.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * 
 * OAuth2 로그인이 최종 성공했을 때 호출됩니다.
 * PrincipalDetails에서 User 정보를 추출하여 JWT 토큰을 생성하고,
 * 프론트엔드로 리다이렉트합니다.
 * 
 * 인증 성공 후 OAuth2 인증 관련 쿠키를 삭제합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    
    @Value("${oauth2.redirect-uri:http://localhost:5173/oauth-redirect}")
    private String redirectUri;

    /** 앱에서 시작했을 때 돌아갈 곳. 커스텀 스킴이라 웹 주소와 섞이지 않는다 */
    @Value("${oauth2.app-redirect-uri:cuddlemarket://oauth}")
    private String appRedirectUri;

    @PostConstruct
    public void init() {
        log.info("OAuth2LoginSuccessHandler 초기화: oauth2.redirect-uri={}, oauth2.app-redirect-uri={}",
                redirectUri, appRedirectUri);
    }
    
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        // 1. PrincipalDetails에서 User 정보 추출
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        User user = principalDetails.getUser();
        
        // 2. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getEmail(),
                user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getEmail(),
                user.getRole().name()
        );
        
        // 3. 어디로 돌아갈지 먼저 정한다 — 쿠키를 지우기 **전에** 읽어야 한다.
        //    (지우고 읽으면 깃발이 사라져 앱에서 시작해도 늘 웹으로 간다)
        boolean fromApp = CookieUtils.getCookie(
                        request,
                        HttpCookieOAuth2AuthorizationRequestRepository.CLIENT_PARAM_COOKIE_NAME
                )
                .map(cookie -> "app".equals(cookie.getValue()))
                .orElse(false);
        String target = fromApp ? appRedirectUri : redirectUri;

        // 4. OAuth2 인증 관련 쿠키 삭제
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 5. 돌려보낸다 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = String.format(
                "%s?accessToken=%s&refreshToken=%s",
                target,
                accessToken,
                refreshToken
        );

        log.info("OAuth2 로그인 성공: email={}, provider={}, fromApp={}, target={}, redirectUrl={}",
                user.getEmail(), user.getProvider(), fromApp, target, redirectUrl);

        // 6. 응답이 이미 커밋되었는지 확인
        if (response.isCommitted()) {
            log.warn("Response already committed, cannot redirect to: {}", redirectUrl);
            return;
        }

        // 7. 리다이렉트
        response.sendRedirect(redirectUrl);

        // 8. 리다이렉트 후 필터 체인 처리를 중단하기 위해 명시적으로 완료
        // 이렇게 하면 필터 체인에서 추가 처리를 시도하지 않음
    }
}

