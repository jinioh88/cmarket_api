package org.cmarket.cmarket.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스
 * 
 * 주요 기능:
 * - Access Token 생성: 짧은 만료 시간 (기본 1시간)
 * - Refresh Token 생성: 긴 만료 시간 (기본 7일)
 * - 토큰 검증: 유효성 및 만료 여부 확인
 * - 인증 정보 추출: 토큰에서 사용자 정보를 추출하여 Authentication 객체 생성
 */
@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds:3600}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds:604800}") long refreshTokenValidityInSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }
    
    /**
     * Access Token 생성
     * 
     * @param email 사용자 이메일
     * @param role 사용자 권한 (예: "USER", "ADMIN")
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidityInMilliseconds);
        
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Refresh Token 생성
     * 
     * @param email 사용자 이메일
     * @param role 사용자 권한 (예: "USER", "ADMIN")
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidityInMilliseconds);
        
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 토큰에서 Claims 추출
     * 
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 토큰에서 이메일 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }
    
    /**
     * 토큰에서 권한 추출
     * 
     * @param token JWT 토큰
     * @return 사용자 권한
     */
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }
    
    /**
     * 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            // 서명이 유효하지 않음
            return false;
        } catch (ExpiredJwtException e) {
            // 토큰이 만료됨
            return false;
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 토큰 형식
            return false;
        } catch (MalformedJwtException e) {
            // 잘못된 토큰 형식
            return false;
        } catch (IllegalArgumentException e) {
            // 토큰이 비어있음
            return false;
        }
    }
    
    /**
     * 토큰에서 인증 정보 추출하여 Authentication 객체 생성
     * 
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
        
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                authorities
        );
    }
    
    /**
     * 토큰 만료 시간 추출
     * 
     * @param token JWT 토큰
     * @return 만료 시간 (Date)
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaims(token).getExpiration();
    }
}

