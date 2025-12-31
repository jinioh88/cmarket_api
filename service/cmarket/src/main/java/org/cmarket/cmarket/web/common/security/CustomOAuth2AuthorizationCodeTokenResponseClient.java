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
        log.info("Authorization Code: {}", 
                authorizationCodeGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode());
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
     * Client Secret을 마스킹하여 로그에 출력
     */
    private String maskSecret(String secret) {
        if (secret == null || secret.length() <= 8) {
            return "****";
        }
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }
}

