package org.cmarket.cmarket.web.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.cmarket.cmarket.web.common.response.ErrorResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    private final org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository tokenBlacklistRepository;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    
    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository tokenBlacklistRepository,
            AuthenticationConfiguration authenticationConfiguration,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.authenticationConfiguration = authenticationConfiguration;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
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
            
            // 인증/인가 실패 시 401/403 JSON 에러 응답
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> {
                    String traceId = getOrCreateTraceId();
                    ErrorResponse errorResponse = new ErrorResponse(
                            ResponseCode.UNAUTHORIZED,
                            "인증이 필요합니다.",
                            traceId
                    );
                    
                    writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String traceId = getOrCreateTraceId();
                    ErrorResponse errorResponse = new ErrorResponse(
                            ResponseCode.FORBIDDEN,
                            "접근 권한이 없습니다.",
                            traceId
                    );
                    
                    writeErrorResponse(response, HttpStatus.FORBIDDEN, errorResponse);
                })
            )
            
            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
            )
            
            // 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 엔드포인트
                .requestMatchers(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/api/auth/email/**",
                    "/api/auth/password/reset/**",  // 비밀번호 재설정 엔드포인트
                    "/api/auth/nickname/check",  // 닉네임 중복 확인 엔드포인트
                    "/oauth2/**",  // OAuth2 로그인 엔드포인트
                    "/login/oauth2/**",  // OAuth2 로그인 리다이렉트 엔드포인트
                    "/actuator/health",
                    "/error"
                ).permitAll()
                
                // 상품 관련 GET 요청은 인증 불필요 (목록, 상세 조회)
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                
                // 이미지 조회는 인증 불필요
                .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                
                // 커뮤니티 게시글 조회는 인증 불필요 (목록, 상세 조회, 댓글 조회)
                .requestMatchers(HttpMethod.GET, "/api/community/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/community/comments/**").permitAll()
                
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
    
    private void writeErrorResponse(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            ErrorResponse errorResponse
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 직렬화를 위한 모듈 등록
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
    
    private String getOrCreateTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }
        return traceId;
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
                "http://localhost:8080",  // 필요시 추가
                "http://localhost:5173",  // 외부 개발자 환경
                "https://localhost:5173"
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
