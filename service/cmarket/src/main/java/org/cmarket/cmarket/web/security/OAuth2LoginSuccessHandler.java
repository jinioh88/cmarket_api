package org.cmarket.cmarket.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러
 * 
 * OAuth2 로그인이 최종 성공했을 때 호출됩니다.
 * PrincipalDetails에서 User 정보를 추출하여 JWT 토큰을 생성하고,
 * 프론트엔드로 리다이렉트합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${oauth2.redirect-uri:http://localhost:3000/oauth-redirect}")
    private String redirectUri;
    
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
        
        // 3. 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = String.format(
                "%s?accessToken=%s&refreshToken=%s",
                redirectUri,
                accessToken,
                refreshToken
        );
        
        log.info("OAuth2 로그인 성공: email={}, provider={}", user.getEmail(), user.getProvider());
        
        // 4. 리다이렉트
        response.sendRedirect(redirectUrl);
    }
}

