package org.cmarket.cmarket.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정
 * 
 * JWT 기반 인증을 위한 핵심 설정:
 * - STATELESS 세션 정책: 세션을 사용하지 않고 JWT만 사용
 * - 기본 로그인 폼 비활성화: REST API이므로 폼 로그인 불필요
 * - CSRF 비활성화: JWT 방식은 세션을 사용하지 않으므로 CSRF 공격에 취약하지 않음
 * - CORS 설정: 프론트엔드와의 통신을 위해 CORS 허용
 * - 필터 등록: JWT 인증 필터를 필터 체인에 추가
 * - 접근 권한 설정: 인증이 필요한 엔드포인트와 허용 엔드포인트 구분
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final org.cmarket.cmarket.domain.repository.TokenBlacklistRepository tokenBlacklistRepository;
    private final AuthenticationConfiguration authenticationConfiguration;
    
    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            org.cmarket.cmarket.domain.repository.TokenBlacklistRepository tokenBlacklistRepository,
            AuthenticationConfiguration authenticationConfiguration
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.authenticationConfiguration = authenticationConfiguration;
    }
    
    /**
     * AuthenticationManager 빈 등록
     * 
     * 로그인 시 사용자 인증을 위해 사용됩니다.
     * Spring Security 6에서는 AuthenticationConfiguration을 통해 자동으로 설정됩니다.
     * 
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 방식은 세션을 사용하지 않으므로 CSRF 공격에 취약하지 않음)
            .csrf(csrf -> csrf.disable())
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 비활성화 (STATELESS)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 기본 로그인 폼 비활성화 (REST API이므로 폼 로그인 불필요)
            .formLogin(formLogin -> formLogin.disable())
            
            // HTTP Basic 인증 비활성화
            .httpBasic(httpBasic -> httpBasic.disable())
            
            // 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 엔드포인트
                .requestMatchers(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/api/auth/email/**",
                    "/actuator/health",
                    "/error"
                ).permitAll()
                
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );
        
        // JWT 인증 필터 등록
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtTokenProvider,
                tokenBlacklistRepository
        );
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * CORS 설정
     * 프론트엔드와의 통신을 위해 CORS를 허용합니다.
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin (프론트엔드 주소)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",  // 로컬 개발 환경
                "http://localhost:8080"   // 필요시 추가
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));
        
        // 인증 정보(쿠키, Authorization 헤더) 허용
        configuration.setAllowCredentials(true);
        
        // Preflight 요청의 캐시 시간 (초)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
