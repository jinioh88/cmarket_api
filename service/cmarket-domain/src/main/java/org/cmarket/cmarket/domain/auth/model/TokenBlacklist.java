package org.cmarket.cmarket.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 토큰 블랙리스트 엔티티
 * 
 * 로그아웃된 JWT 토큰을 블랙리스트로 관리합니다.
 * - 로그아웃 시 토큰을 블랙리스트에 추가
 * - 이후 해당 토큰으로는 인증 불가능
 * - 토큰 만료 시간과 함께 저장하여 자동 정리 가능
 */
@Entity
@Table(name = "token_blacklist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenBlacklist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;  // JWT 토큰 전체 문자열
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;  // 토큰 만료 시간 (토큰에서 추출)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public TokenBlacklist(
            String token,
            LocalDateTime expiresAt
    ) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}

