package org.cmarket.cmarket.web.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

/**
 * OAuth2 토큰 교환 요청을 로깅하는 커스텀 클라이언트
 * 
 * 실제로 카카오 서버에 전송되는 파라미터를 확인하기 위해 사용됩니다.
 */
@Slf4j
@Component
public class CustomOAuth2AuthorizationCodeTokenResponseClient 
        implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
    
    private final org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient delegate;
    
    public CustomOAuth2AuthorizationCodeTokenResponseClient() {
        this.delegate = new org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient();
    }
    
    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
        // 토큰 교환 요청 정보 로깅
        log.info("========== OAuth2 토큰 교환 요청 ==========");
        log.info("Client Registration ID: {}", 
                authorizationCodeGrantRequest.getClientRegistration().getRegistrationId());
        log.info("Client ID: {}", 
                authorizationCodeGrantRequest.getClientRegistration().getClientId());
        log.info("Client Secret: {} (마스킹됨)", 
                maskSecret(authorizationCodeGrantRequest.getClientRegistration().getClientSecret()));
        log.info("Token URI: {}", 
                authorizationCodeGrantRequest.getClientRegistration().getProviderDetails().getTokenUri());
        // 인가 코드도 마스킹한다. 한 번 쓰면 만료되고 유효기간도 짧아 토큰만큼 위험하진
        // 않지만, 그 자체로 토큰을 받아 올 수 있는 값이라 운영 로그에 통째로 남길 것은 아니다.
        // 앞뒤 네 자리만 남겨 「어느 요청이었나」는 여전히 맞춰 볼 수 있게 한다.
        log.info("Authorization Code: {} (마스킹됨)",
                maskSecret(authorizationCodeGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode()));
        log.info("Redirect URI: {}", 
                authorizationCodeGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getRedirectUri());
        log.info("State: {}", 
                authorizationCodeGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getState());
        log.info("=========================================");
        
        try {
            // 실제 토큰 교환 요청 실행
            OAuth2AccessTokenResponse response = delegate.getTokenResponse(authorizationCodeGrantRequest);
            log.info("OAuth2 토큰 교환 성공");
            return response;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("========== OAuth2 토큰 교환 HTTP 에러 ==========");
            log.error("HTTP 상태 코드: {}", e.getStatusCode());
            log.error("HTTP 응답 본문: {}", e.getResponseBodyAsString());
            log.error("=========================================");
            throw e;
        } catch (Exception e) {
            log.error("OAuth2 토큰 교환 실패: {}", e.getMessage());
            log.error("예외 타입: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("원인 예외: {}", e.getCause().getClass().getName());
                log.error("원인 메시지: {}", e.getCause().getMessage());
            }
            throw e;
        }
    }
    
    /**
     * 비밀 값을 마스킹하여 로그에 출력한다 (Client Secret · 인가 코드).
     *
     * 앞 4자 + **** + 뒤 4자. 여덟 자 이하면 통째로 가린다 — 그보다 짧으면
     * 앞뒤를 남기는 순간 원래 값이 거의 드러난다.
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8) {
            return "****";
        }
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }
}

