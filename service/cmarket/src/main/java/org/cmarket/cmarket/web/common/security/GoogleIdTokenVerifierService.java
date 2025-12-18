package org.cmarket.cmarket.web.common.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Google ID Token 검증 서비스
 * 
 * 프론트엔드에서 Google Sign-In으로 받은 ID Token을 검증합니다.
 * ID Token은 Google에서 서명한 JWT로, 사용자 정보를 포함합니다.
 */
@Slf4j
@Service
public class GoogleIdTokenVerifierService {
    
    private final GoogleIdTokenVerifier verifier;
    
    public GoogleIdTokenVerifierService(
            @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId
    ) {
        // Google ID Token 검증기 초기화
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(clientId))
                .build();
        
        log.info("GoogleIdTokenVerifierService initialized with clientId: {}", 
                clientId.substring(0, Math.min(10, clientId.length())) + "...");
    }
    
    /**
     * Google ID Token을 검증하고 사용자 정보를 반환합니다.
     *
     * @param idTokenString 프론트엔드에서 받은 ID Token 문자열
     * @return 검증된 사용자 정보 (null이면 검증 실패)
     */
    public GoogleUserInfo verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken == null) {
                log.warn("Google ID Token 검증 실패: 유효하지 않은 토큰");
                return null;
            }
            
            GoogleIdToken.Payload payload = idToken.getPayload();
            
            // 필수 필드 검증
            String email = payload.getEmail();
            if (email == null || email.isEmpty()) {
                log.warn("Google ID Token에 이메일 정보가 없습니다.");
                return null;
            }
            
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                log.warn("Google 계정의 이메일이 인증되지 않았습니다: {}", email);
                return null;
            }
            
            // 사용자 정보 추출
            String socialId = payload.getSubject();  // Google 고유 사용자 ID
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            
            log.info("Google ID Token 검증 성공: email={}", email);
            
            return new GoogleUserInfo(
                    socialId,
                    email,
                    name,
                    picture
            );
            
        } catch (GeneralSecurityException | IOException e) {
            log.error("Google ID Token 검증 중 오류 발생", e);
            return null;
        }
    }
    
    /**
     * Google 사용자 정보를 담는 레코드
     */
    public record GoogleUserInfo(
            String socialId,    // Google 고유 사용자 ID (sub)
            String email,       // 이메일
            String name,        // 이름 (nullable)
            String picture      // 프로필 사진 URL (nullable)
    ) {}
}
