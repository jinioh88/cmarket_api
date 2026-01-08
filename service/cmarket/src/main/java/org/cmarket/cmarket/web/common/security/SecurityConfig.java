package org.cmarket.cmarket.web.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.cmarket.cmarket.web.common.response.ErrorResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository tokenBlacklistRepository;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final CustomOAuth2AuthorizationCodeTokenResponseClient customOAuth2AuthorizationCodeTokenResponseClient;
    
    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository tokenBlacklistRepository,
            AuthenticationConfiguration authenticationConfiguration,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository,
            CustomOAuth2AuthorizationCodeTokenResponseClient customOAuth2AuthorizationCodeTokenResponseClient
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.authenticationConfiguration = authenticationConfiguration;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.customOAuth2AuthorizationCodeTokenResponseClient = customOAuth2AuthorizationCodeTokenResponseClient;
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
                    String requestPath = request.getRequestURI();
                    
                    // 응답이 이미 커밋된 경우 처리하지 않음
                    if (response.isCommitted()) {
                        log.warn("[{}] Authentication required but response already committed: {}", traceId, requestPath);
                        return;
                    }
                    
                    // SSE나 WebSocket 엔드포인트에서 발생한 경우 별도 처리
                    if (requestPath != null && 
                        (requestPath.contains("/notifications/stream") || 
                         requestPath.contains("/ws-stomp"))) {
                        log.warn("[{}] Authentication required on streaming endpoint (response may be committed): {}", 
                                traceId, requestPath);
                        return;
                    }
                    
                    ErrorResponse errorResponse = new ErrorResponse(
                            ResponseCode.UNAUTHORIZED,
                            "인증이 필요합니다.",
                            traceId
                    );
                    
                    writeErrorResponse(response, HttpStatus.UNAUTHORIZED, errorResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String traceId = getOrCreateTraceId();
                    String requestPath = request.getRequestURI();
                    String requestMethod = request.getMethod();
                    
                    // 항상 로그 출력 (요청 경로 확인용)
                    log.warn("[{}] AccessDeniedException 발생: method={}, path={}, committed={}", 
                            traceId, requestMethod, requestPath, response.isCommitted());
                    
                    // 응답이 이미 커밋된 경우 처리하지 않음
                    if (response.isCommitted()) {
                        log.warn("[{}] Access denied but response already committed: method={}, path={}", 
                                traceId, requestMethod, requestPath);
                        return;
                    }
                    
                    // SSE나 WebSocket 엔드포인트에서 발생한 경우 별도 처리
                    if (requestPath != null && 
                        (requestPath.contains("/notifications/stream") || 
                         requestPath.contains("/ws-stomp"))) {
                        log.warn("[{}] Access denied on streaming endpoint (response may be committed): method={}, path={}", 
                                traceId, requestMethod, requestPath);
                        return;
                    }
                    
                    ErrorResponse errorResponse = new ErrorResponse(
                            ResponseCode.FORBIDDEN,
                            "접근 권한이 없습니다.",
                            traceId
                    );
                    
                    writeErrorResponse(response, HttpStatus.FORBIDDEN, errorResponse);
                })
            )
            
            // OAuth2 로그인 설정
            // JWT 기반 STATELESS 세션 정책에서 OAuth2를 사용하기 위해
            // 세션 대신 쿠키에 인증 요청 정보를 저장합니다.
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorization")
                    .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/login/oauth2/code/*")
                )
                .tokenEndpoint(token -> token
                    .accessTokenResponseClient(customOAuth2AuthorizationCodeTokenResponseClient)
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    // OAuth2 인증 실패 로그
                    log.error("========== OAuth2 로그인 실패 ==========");
                    log.error("요청 URI: {}", request.getRequestURI());
                    log.error("요청 쿼리: {}", request.getQueryString());
                    log.error("요청 URL: {}://{}{}", 
                            request.getScheme(), 
                            request.getServerName(), 
                            request.getRequestURI());
                    log.error("Host 헤더: {}", request.getHeader("Host"));
                    log.error("X-Forwarded-Host 헤더: {}", request.getHeader("X-Forwarded-Host"));
                    log.error("X-Forwarded-Proto 헤더: {}", request.getHeader("X-Forwarded-Proto"));
                    log.error("X-Forwarded-Port 헤더: {}", request.getHeader("X-Forwarded-Port"));
                    log.error("예외 타입: {}", exception.getClass().getName());
                    log.error("오류 메시지: {}", exception.getMessage());
                    
                    // OAuth2AuthenticationException의 경우 상세 정보 추출
                    String errorCode = null;
                    String errorDescription = null;
                    String errorUri = null;
                    if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
                        org.springframework.security.oauth2.core.OAuth2Error error = 
                            ((org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception).getError();
                        errorCode = error.getErrorCode();
                        errorDescription = error.getDescription();
                        errorUri = error.getUri();
                        log.error("OAuth2 Error Code: {}", errorCode);
                        log.error("OAuth2 Error Description: {}", errorDescription);
                        log.error("OAuth2 Error URI: {}", errorUri);
                    }
                    
                    // 원인 예외가 있으면 출력
                    Throwable cause = exception.getCause();
                    if (cause != null) {
                        log.error("원인 예외: {}", cause.getClass().getName());
                        log.error("원인 메시지: {}", cause.getMessage());
                        if (cause instanceof org.springframework.web.client.HttpClientErrorException) {
                            org.springframework.web.client.HttpClientErrorException httpException = 
                                (org.springframework.web.client.HttpClientErrorException) cause;
                            log.error("HTTP 상태 코드: {}", httpException.getStatusCode());
                            log.error("HTTP 응답 본문: {}", httpException.getResponseBodyAsString());
                        }
                    }
                    
                    exception.printStackTrace();
                    log.error("=========================================");
                    
                    // 에러 메시지 추출 (null 방지)
                    String errorMessage = exception.getMessage();
                    if (errorMessage == null || errorMessage.isBlank()) {
                        // OAuth2AuthenticationException의 경우 OAuth2Error에서 description 추출
                        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
                            org.springframework.security.oauth2.core.OAuth2Error error = 
                                ((org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception).getError();
                            errorMessage = error.getDescription();
                        }
                        // 여전히 null이면 기본 메시지 사용
                        if (errorMessage == null || errorMessage.isBlank()) {
                            errorMessage = "인증에 실패했습니다";
                        }
                    }
                    
                    // 프론트엔드 로그인 페이지로 리다이렉트 (오류 정보 포함)
                    // 환경 변수에서 가져오거나 기본값 사용
                    String frontendUrl = System.getenv("FRONTEND_URL");
                    if (frontendUrl == null || frontendUrl.isBlank()) {
                        frontendUrl = "https://cuddle-market.duckdns.org";
                    }
                    String redirectUrl = frontendUrl + "/login?error=oauth2_failed&message="
                            + java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8);
                    response.sendRedirect(redirectUrl);
                })
            )
            
            // 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // OPTIONS 요청은 CORS preflight를 위해 항상 허용
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 인증 없이 접근 가능한 엔드포인트
                .requestMatchers(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/api/auth/google",  // Google ID Token 로그인 엔드포인트
                    "/api/auth/email/**",
                    "/api/auth/password/reset/**",  // 비밀번호 재설정 엔드포인트
                    "/api/auth/nickname/check",  // 닉네임 중복 확인 엔드포인트
                    "/login",  // Spring Security 기본 로그인 페이지 (OAuth2 실패 시 리다이렉트)
                    "/login/**",  // 로그인 관련 엔드포인트
                    "/oauth2/**",  // OAuth2 로그인 엔드포인트
                    "/login/oauth2/**",  // OAuth2 로그인 리다이렉트 엔드포인트
                    "/ws-stomp/**",  // WebSocket 엔드포인트 (인증은 STOMP 레벨에서 처리)
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
        // 응답이 이미 커밋된 경우 처리하지 않음
        if (response.isCommitted()) {
            log.warn("Response already committed, cannot write error response");
            return;
        }
        
        try {
            response.setStatus(status.value());
            response.setContentType("application/json;charset=UTF-8");
            
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 직렬화를 위한 모듈 등록
            objectMapper.writeValue(response.getWriter(), errorResponse);
        } catch (IllegalStateException e) {
            // 응답이 커밋된 후에 setStatus나 writeValue를 호출한 경우
            log.warn("Failed to write error response (response may be committed): {}", e.getMessage());
        }
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
        
        // 허용할 Origin 패턴 (프론트엔드 주소)
        // Spring Boot 3.x에서는 setAllowCredentials(true)와 함께 사용할 때 
        // setAllowedOriginPatterns를 사용해야 합니다.
        // Vercel 배포: 프리뷰 배포와 프로덕션 배포 모두 지원하기 위해 와일드카드 패턴 사용
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",  // 로컬 개발 환경
                "http://localhost:8080",  // 필요시 추가
                "http://localhost:5173",  // 외부 개발자 환경
                "https://localhost:5173",
                "https://*.vercel.app",  // Vercel 모든 서브도메인 (프리뷰 + 프로덕션)
                "https://cuddle-market-fe.vercel.app",  // 프론트엔드 프로덕션 환경 (명시적)
                "https://cuddle-market.duckdns.org"  // 프론트엔드 프로덕션 환경 (Duck DNS),
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
