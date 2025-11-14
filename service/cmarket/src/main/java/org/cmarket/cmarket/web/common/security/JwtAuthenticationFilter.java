package org.cmarket.cmarket.web.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 
 * 클라이언트가 API 요청 시 헤더에 `Authorization: Bearer <TOKEN>`을 담아 보내면,
 * 이 필터가 토큰을 가로채 검증합니다.
 * 
 * 동작:
 * 1. Authorization 헤더에서 JWT 토큰 추출
 * 2. 토큰 유효성 검증 (JwtTokenProvider 사용)
 * 3. 블랙리스트 토큰 검증 (TokenBlacklistRepository 사용)
 * 4. 토큰이 유효하면 SecurityContextHolder에 인증 정보(Authentication) 저장
 * 5. 토큰이 없거나 유효하지 않으면 필터 통과 (인증 실패는 SecurityConfig에서 처리)
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    
    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistRepository tokenBlacklistRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Authorization 헤더에서 토큰 추출
        String token = resolveToken(request);
        
        // 2. 토큰이 있고 유효한 경우에만 인증 처리
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 3. 블랙리스트 토큰 검증
            if (!tokenBlacklistRepository.existsByToken(token)) {
                // 4. 토큰에서 인증 정보 추출
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                
                // 5. SecurityContextHolder에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        // 6. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
    
    /**
     * Authorization 헤더에서 JWT 토큰 추출
     * 
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
}

