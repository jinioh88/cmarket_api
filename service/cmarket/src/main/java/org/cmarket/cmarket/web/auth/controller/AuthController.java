package org.cmarket.cmarket.web.auth.controller;

import org.cmarket.cmarket.domain.auth.app.dto.EmailVerificationVerifyCommand;
import org.cmarket.cmarket.domain.auth.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.auth.app.dto.PasswordChangeCommand;
import org.cmarket.cmarket.domain.auth.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.auth.app.dto.WithdrawalCommand;
import org.cmarket.cmarket.domain.auth.app.service.AuthService;
import org.cmarket.cmarket.domain.auth.app.service.EmailVerificationService;
import org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository;
import org.cmarket.cmarket.web.auth.dto.EmailVerificationSendRequest;
import org.cmarket.cmarket.web.auth.dto.EmailVerificationVerifyRequest;
import org.cmarket.cmarket.web.auth.dto.LoginRequest;
import org.cmarket.cmarket.web.auth.dto.LoginResponse;
import org.cmarket.cmarket.web.auth.dto.PasswordChangeRequest;
import org.cmarket.cmarket.web.auth.dto.PasswordResetRequest;
import org.cmarket.cmarket.web.auth.dto.AccountFindRequest;
import org.cmarket.cmarket.web.auth.dto.PasswordResetSendRequest;
import org.cmarket.cmarket.web.auth.dto.RefreshTokenRequest;
import org.cmarket.cmarket.web.auth.dto.SignUpRequest;
import org.cmarket.cmarket.web.auth.dto.TokenRefreshResponse;
import org.cmarket.cmarket.web.auth.dto.UserWebDto;
import org.cmarket.cmarket.web.auth.dto.WithdrawalRequest;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.JwtTokenProvider;
import org.cmarket.cmarket.web.common.security.RateLimiter;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

/**
 * 인증 관련 컨트롤러
 * 
 * 회원가입, 로그인, 이메일 인증 등의 인증 관련 API를 제공합니다.
 */
@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;  // 검증용으로만 사용
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final org.cmarket.cmarket.web.auth.service.GoogleAuthService googleAuthService;
    private final RateLimiter rateLimiter;

    // ── 횟수 제한 한도 (#849) ────────────────────────────────────────────
    // ⚠️ **넉넉하게 잡았다.** 여기 걸리면 정상 사용자가 못 쓰게 되는데, 계정 열거는
    //    수천~수만 번 두드려야 쓸모가 생기는 공격이라 굳이 빡빡할 까닭이 없다.
    //    막혔을 때 무엇을 돌려주는지가 자리마다 다르다 — 아래 각 메서드 주석 참고.
    private static final Duration MAIL_WINDOW = Duration.ofHours(1);
    private static final int MAIL_LIMIT_PER_IP = 10;
    private static final int MAIL_LIMIT_PER_EMAIL = 5;

    private static final Duration CHECK_WINDOW = Duration.ofMinutes(1);
    private static final int CHECK_LIMIT_PER_IP = 60;

    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    private static final int LOGIN_LIMIT_PER_IP = 30;
    private static final int LOGIN_LIMIT_PER_EMAIL = 10;
    
    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            EmailVerificationService emailVerificationService,
            TokenBlacklistRepository tokenBlacklistRepository,
            org.cmarket.cmarket.web.auth.service.GoogleAuthService googleAuthService,
            RateLimiter rateLimiter
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailVerificationService = emailVerificationService;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.googleAuthService = googleAuthService;
        this.rateLimiter = rateLimiter;
    }
    
    /**
     * 이메일 중복 확인
     *
     * GET /api/auth/email/check?email={email}
     *
     * 이메일이 이미 사용 중인지 확인합니다.
     *
     * @param email 확인할 이메일
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     */
    @GetMapping("/email/check")
    public ResponseEntity<SuccessResponse<Boolean>> checkEmail(
            @RequestParam @Email String email,
            HttpServletRequest httpRequest
    ) {
        // ⚠️ **여기는 가입 여부를 일부러 알려주는 자리다.** 가입 화면에서 중복을 안 알려 주면
        //    사람이 「왜 가입이 안 되지」에 갇힌다 — 쓸모가 안전보다 앞선다고 정했다(#849).
        //    대신 **대량으로 훑는 것**을 횟수로 막는다.
        //
        // ⚠️ 여기서는 429 를 줘도 된다. 「막힐 만큼 눌렀다」는 신호이지 「이 이메일이
        //    회원이다」라는 신호가 아니다. 오히려 조용히 통과시키면 화면이 잘못된 답을 믿는다.
        if (!rateLimiter.tryConsumeIp("email-check:ip", httpRequest.getRemoteAddr(), CHECK_LIMIT_PER_IP, CHECK_WINDOW)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new SuccessResponse<>(
                            ResponseCode.TOO_MANY_REQUESTS,
                            "요청이 너무 잦습니다. 잠시 후 다시 시도해주세요.",
                            null
                    ));
        }

        boolean isAvailable = authService.isEmailAvailable(email);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.",
                        isAvailable
                ));
    }
    
    /**
     * 이메일 인증코드 발송
     * 
     * POST /api/auth/email/verification/send
     * 
     * 회원가입 시 이메일 인증을 위해 인증코드를 발송합니다.
     * - 6자리 랜덤 인증코드 생성
     * - 인증코드는 5분간 유효
     * - 이메일로 인증코드 발송
     */
    @PostMapping("/email/verification/send")
    public ResponseEntity<SuccessResponse<String>> sendVerificationCode(
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        // 앱 서비스 호출 (인증코드 생성 및 이메일 발송)
        //
        // ⚠️ 인증코드를 응답에 담지 않는다. 담으면 메일함에 접근하지 못하는 사람도
        //    코드를 손에 넣어, 남의 이메일 주소로 가입까지 할 수 있다.
        //    (메일 발송이 안 되던 동안 테스트하려고 담아 두었던 것이다)
        authService.sendEmailVerificationCode(request.getEmail());
        
        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "인증코드를 발송했습니다."
                ));
    }
    
    /**
     * 이메일 인증코드 검증
     * 
     * POST /api/auth/email/verification/verify
     * 
     * 사용자가 입력한 인증코드를 검증합니다.
     * - 이메일과 인증코드로 조회
     * - 만료 여부 확인
     * - 인증 완료 처리
     */
    @PostMapping("/email/verification/verify")
    public ResponseEntity<SuccessResponse<String>> verifyVerificationCode(
            @Valid @RequestBody EmailVerificationVerifyRequest request
    ) {
        // 웹 DTO → 앱 DTO 변환
        EmailVerificationVerifyCommand command = EmailVerificationVerifyCommand.builder()
                .email(request.getEmail())
                .verificationCode(request.getVerificationCode())
                .build();
        
        // 인증코드 검증
        boolean isValid = emailVerificationService.verifyCode(command);
        
        if (!isValid) {
            // 검증 실패 시 에러 응답 (GlobalExceptionHandler에서 처리하도록 예외 발생)
            throw new IllegalArgumentException("만료된 인증코드입니다. 인증코드 전송 재시도 부탁드립니다.");
        }
        
        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "인증이 완료되었습니다."
                ));
    }
    
    /**
     * 회원가입
     * 
     * POST /api/auth/signup
     * 
     * 사용자가 입력한 정보로 회원가입을 처리합니다.
     * - 이메일 중복 검증
     * - 닉네임 중복 검증
     * - 만 14세 이상 검증
     * - 비밀번호 암호화 후 User 엔티티 저장
     * 
     * 참고: 이메일 인증코드 검증은 프론트엔드에서 회원가입 버튼을 누르기 전에 완료됩니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<UserWebDto>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        // 웹 DTO → 앱 DTO 변환
        SignUpCommand command = SignUpCommand.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .addressSido(request.getAddressSido())
                .addressGugun(request.getAddressGugun())
                // 약관 동의 (#1088). 옛 앱은 안 보내므로 null 로 간다
                .termsAgreed(request.getTermsAgreed())
                .privacyAgreed(request.getPrivacyAgreed())
                .build();
        
        // 앱 서비스 호출
        org.cmarket.cmarket.domain.auth.app.dto.UserDto userDto = authService.signUp(command);
        
        // 앱 DTO → 웹 DTO 변환
        UserWebDto userWebDto = UserWebDto.fromDto(userDto);
        
        // 응답 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(
                        ResponseCode.CREATED,
                        userWebDto
                ));
    }
    
    /**
     * 로그인
     * 
     * POST /api/auth/login
     * 
     * 이메일과 비밀번호로 로그인을 처리합니다.
     * - AuthenticationManager를 사용하여 사용자 인증
     * - 인증 성공 시 JWT Access Token과 Refresh Token 생성
     * - 사용자 정보와 토큰을 함께 반환
     */
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        // ⚠️ **로그인은 지금도 열거가 안 된다** — 없는 계정이든 비밀번호가 틀렸든 같은 문구를
        //    준다. 여기 제한을 두는 것은 열거보다 **비밀번호 무차별 대입**을 늦추기 위해서다.
        //
        // ⚠️ 한도를 넉넉히 잡았다(IP 분당 30 · 계정 분당 10). 여기 걸리면 정상 사용자가
        //    로그인을 못 한다 — 특히 **한 곳에서 여러 사람이 테스트할 때** 같은 IP 로 묶인다.
        //    출시 전 테스터 기간에 문제가 되면 이 블록만 걷어내면 된다.
        if (!(rateLimiter.tryConsumeIp("login:ip", httpRequest.getRemoteAddr(), LOGIN_LIMIT_PER_IP, LOGIN_WINDOW)
                & rateLimiter.tryConsume("login:email", request.getEmail(), LOGIN_LIMIT_PER_EMAIL, LOGIN_WINDOW))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new SuccessResponse<>(
                            ResponseCode.TOO_MANY_REQUESTS,
                            "로그인 시도가 너무 잦습니다. 잠시 후 다시 시도해주세요.",
                            null
                    ));
        }

        // 웹 DTO → 앱 DTO 변환
        LoginCommand command = LoginCommand.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        
        // 앱 서비스 호출 (사용자 조회 및 비밀번호 검증)
        org.cmarket.cmarket.domain.auth.app.dto.LoginResponse loginResponse = authService.login(command);
        
        // AuthenticationManager를 사용하여 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // JWT 토큰 생성
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        
        String accessToken = jwtTokenProvider.createAccessToken(request.getEmail(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getEmail(), role);
        
        // 앱 DTO → 웹 DTO 변환
        UserWebDto userWebDto = UserWebDto.fromDto(loginResponse.getUser());
        
        // 웹 응답 생성
        LoginResponse loginWebResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userWebDto)
                .build();
        
        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        loginWebResponse
                ));
    }
    
    /**
     * 로그아웃
     * 
     * POST /api/auth/logout
     * 
     * 현재 사용 중인 JWT 토큰을 블랙리스트에 등록하여 무효화합니다.
     * - Authorization 헤더에서 토큰 추출
     * - 토큰을 TokenBlacklist에 저장
     * - 이후 해당 토큰으로는 인증 불가능
     * 
     * 참고: POST 메서드를 사용하는 이유는 로그아웃이 서버 상태를 변경하는 작업이기 때문입니다.
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        // 1. Authorization 헤더에서 토큰 추출
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 필요합니다.");
        }
        
        String token = authorization.substring(7);  // "Bearer " 제거
        
        // 2. 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        
        // 3. 토큰에서 만료 시간 추출
        java.util.Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.ofInstant(
                expirationDate.toInstant(),
                java.time.ZoneId.systemDefault()
        );
        
        // 4. 앱 서비스 호출 (토큰을 블랙리스트에 추가)
        authService.logout(token, expiresAt);
        
        // 5. 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "로그아웃되었습니다."
                ));
    }
    
    /**
     * Access Token 갱신
     * 
     * POST /api/auth/refresh
     * 
     * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.
     * - Refresh Token 유효성 검증
     * - Refresh Token이 블랙리스트에 있는지 확인
     * - 새로운 Access Token과 Refresh Token 생성
     * 
     * @param request Refresh Token 요청
     * @return 새로운 Access Token과 Refresh Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        String refreshToken = request.getRefreshToken();
        
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        
        // 2. Refresh Token이 블랙리스트에 있는지 확인
        if (tokenBlacklistRepository.existsByToken(refreshToken)) {
            throw new IllegalArgumentException("이미 로그아웃된 Refresh Token입니다.");
        }
        
        // 3. Refresh Token에서 사용자 정보 추출
        String email = jwtTokenProvider.getEmailFromRefreshToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromRefreshToken(refreshToken);
        
        // 4. 새로운 Access Token과 Refresh Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(email, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email, role);
        
        // 5. 기존 Refresh Token을 블랙리스트에 추가 (토큰 재사용 방지)
        // 만료된 토큰이어도 claims는 추출 가능하므로 getClaimsFromExpiredToken 사용
        io.jsonwebtoken.Claims claims = jwtTokenProvider.getClaimsFromExpiredToken(refreshToken);
        java.util.Date expirationDate = claims.getExpiration();
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.ofInstant(
                expirationDate.toInstant(),
                java.time.ZoneId.systemDefault()
        );
        authService.logout(refreshToken, expiresAt);
        
        // 6. 응답 생성
        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        
        // 7. 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        response
                ));
    }
    
    /**
     * 비밀번호 재설정 인증코드 발송
     * 
     * 사용자가 비밀번호를 잊었을 때, 이메일로 인증코드를 발송합니다.
     * 
     * @param request 비밀번호 재설정 인증코드 발송 요청
     * @return 성공 응답
     */
    /**
     * 계정 찾기 — 가입 방법을 메일로 안내한다 (#849)
     *
     * ⚠️ **회원이든 아니든 똑같은 200 과 똑같은 문구를 돌려준다.** 갈라 말하는 순간
     *    남의 이메일을 넣어 본 사람이 회원 여부를 알아낼 수 있다(계정 열거).
     *    진짜 안내는 메일로 간다 — 메일함을 여는 사람은 그 이메일의 주인뿐이다.
     *
     * ⚠️ SecurityConfig 의 permitAll 목록에 "/api/auth/account/find" 가 있어야 한다.
     *    빠지면 401 이 나는데 **화면은 그래도 「보냈습니다」라고 말하므로 아무도 못 알아챈다.**
     *
     * @param request 계정 찾기 요청
     * @return 성공 응답 (가입 여부와 무관하게 같다)
     */
    @PostMapping("/account/find")
    public ResponseEntity<SuccessResponse<String>> findAccount(
            @Valid @RequestBody AccountFindRequest request,
            HttpServletRequest httpRequest
    ) {
        // ⚠️ **막혔을 때도 같은 200 을 준다.** 429 를 주면 「막힐 만큼 눌렀다」가 또 하나의
        //    신호가 되고, 무엇보다 화면은 어차피 서버 응답을 안 보므로 사용자에게는
        //    똑같이 보인다. 메일만 안 나간다.
        // ⚠️ `&&` 가 아니라 `&` 다. 앞이 false 여도 뒤를 반드시 세야 한다 —
        //    `&&` 로 건너뛰면 IP 한도에 걸린 동안 이메일 쪽 계수기가 멈춘다.
        boolean allowed =
                rateLimiter.tryConsumeIp("account-find:ip", httpRequest.getRemoteAddr(), MAIL_LIMIT_PER_IP, MAIL_WINDOW)
                        & rateLimiter.tryConsume("account-find:email", request.getEmail(), MAIL_LIMIT_PER_EMAIL, MAIL_WINDOW);

        if (allowed) {
            // 앱 서비스 호출 — 없는 이메일이어도 예외를 던지지 않는다
            authService.sendAccountMethodNotice(request.getEmail());
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "가입된 계정이 있다면 안내 메일을 보냈습니다."
                ));
    }
    
    @PostMapping("/password/reset/send")
    public ResponseEntity<SuccessResponse<String>> sendPasswordResetCode(
            @Valid @RequestBody PasswordResetSendRequest request,
            HttpServletRequest httpRequest
    ) {
        // 1. 앱 서비스 호출 (이메일로 사용자 조회 및 인증코드 발송)
        //
        // ⚠️ 인증코드를 응답에 담지 않는다. 이쪽은 계정 탈취로 이어진다 —
        //    코드를 얻으면 verify를 통과시킬 수 있고, resetPassword는 인증 여부만
        //    보므로 남의 이메일 주소만 알면 비밀번호를 바꿀 수 있게 된다.
        // ⚠️ 막혔을 때도 같은 200 이다(계정 찾기와 같은 까닭).
        boolean allowed =
                rateLimiter.tryConsumeIp("password-reset:ip", httpRequest.getRemoteAddr(), MAIL_LIMIT_PER_IP, MAIL_WINDOW)
                        & rateLimiter.tryConsume("password-reset:email", request.getEmail(), MAIL_LIMIT_PER_EMAIL, MAIL_WINDOW);

        if (allowed) {
            authService.sendPasswordResetCode(request.getEmail());
        }

        // 2. 응답 반환
        //
        // ⚠️ **문구가 바뀌었다(#849).** 예전에는 「인증코드를 발송했습니다」였는데, 이제
        //    소셜 계정에게는 인증코드가 아니라 가입 방법 안내가 가고, 없는 이메일에는
        //    아무것도 안 간다. 그 셋이 밖에서 구분되면 안 되므로 문구를 하나로 맞춘다.
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "가입된 계정이 있다면 안내 메일을 보냈습니다."
                ));
    }
    
    /**
     * 비밀번호 재설정 인증코드 검증
     * 
     * POST /api/auth/password/reset/verify
     * 
     * 비밀번호 재설정을 위해 발송된 인증코드를 검증합니다.
     * - 이메일과 인증코드가 일치하는지 확인
     * - 만료 여부 확인
     * - 인증 완료 처리
     * 
     * @param request 인증코드 검증 요청
     * @return 성공 응답
     */
    @PostMapping("/password/reset/verify")
    public ResponseEntity<SuccessResponse<String>> verifyPasswordResetCode(
            @Valid @RequestBody EmailVerificationVerifyRequest request
    ) {
        // 웹 DTO → 앱 DTO 변환
        EmailVerificationVerifyCommand command = EmailVerificationVerifyCommand.builder()
                .email(request.getEmail())
                .verificationCode(request.getVerificationCode())
                .build();
        
        // 인증코드 검증
        boolean isValid = emailVerificationService.verifyCode(command);
        
        if (!isValid) {
            // 검증 실패 시 에러 응답
            throw new IllegalArgumentException("만료된 인증코드이거나 올바르지 않은 인증코드입니다. 인증코드 전송 재시도 부탁드립니다.");
        }
        
        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "인증이 완료되었습니다."
                ));
    }
    
    /**
     * 비밀번호 재설정
     * 
     * 인증코드 검증이 완료된 상태에서 비밀번호를 변경합니다.
     * 
     * @param request 비밀번호 재설정 요청
     * @return 성공 응답
     */
    @PatchMapping("/password/reset")
    public ResponseEntity<SuccessResponse<String>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        // 1. 비밀번호 일치 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 2. 앱 서비스 호출 (이메일 인증 상태 확인 및 비밀번호 변경)
        authService.resetPassword(
                request.getEmail(),
                request.getNewPassword()
        );
        
        // 3. 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "비밀번호가 변경되었습니다."
                ));
    }
    
    /**
     * 비밀번호 변경
     * 
     * 로그인한 사용자가 현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다.
     * 
     * @param request 비밀번호 변경 요청
     * @return 성공 응답
     */
    @PatchMapping("/password/change")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<String>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        // 1. 비밀번호 일치 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 2. 현재 로그인한 사용자의 이메일 추출
        String email = SecurityUtils.getCurrentUserEmail();
        
        // 3. 웹 DTO → 앱 DTO 변환
        PasswordChangeCommand command = PasswordChangeCommand.builder()
                .email(email)
                .currentPassword(request.getCurrentPassword())
                .newPassword(request.getNewPassword())
                .build();
        
        // 4. 앱 서비스 호출 (현재 비밀번호 확인 및 비밀번호 변경)
        authService.changePassword(command);
        
        // 5. 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "비밀번호가 변경되었습니다."
                ));
    }
    
    /**
     * 회원 탈퇴
     * 
     * 현재 로그인한 사용자의 계정을 소프트 삭제 처리합니다.
     * 탈퇴 사유를 저장하고, 즉시 로그아웃 처리됩니다.
     * 
     * @param request 회원 탈퇴 요청
     * @param authentication 현재 인증된 사용자 정보
     * @param authorization Authorization 헤더 (로그아웃 처리용)
     * @return 성공 응답
     */
    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<String>> withdraw(
            @Valid @RequestBody WithdrawalRequest request,
            Authentication authentication,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        // 1. 현재 로그인한 사용자 이메일 추출
        // JwtAuthenticationFilter에서 principal은 email (String)으로 설정됨
        String email = (String) authentication.getPrincipal();
        
        // 2. 웹 DTO → 앱 DTO 변환
        WithdrawalCommand command = WithdrawalCommand.builder()
                .email(email)
                .reason(request.getReason())
                .detailReason(request.getDetailReason())
                .build();
        
        // 3. 앱 서비스 호출 (탈퇴 사유 저장 및 소프트 삭제 처리)
        authService.withdraw(command);
        
        // 4. 로그아웃 처리 (토큰 블랙리스트 추가)
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);  // "Bearer " 제거
            
            // 토큰 유효성 검증
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰에서 만료 시간 추출
                java.util.Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);
                java.time.LocalDateTime expiresAt = java.time.LocalDateTime.ofInstant(
                        expirationDate.toInstant(),
                        java.time.ZoneId.systemDefault()
                );
                
                // 토큰을 블랙리스트에 추가
                authService.logout(token, expiresAt);
            }
        }
        
        // 5. 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "회원 탈퇴가 완료되었습니다."
                ));
    }
    
    /**
     * 닉네임 중복 확인
     * 
     * GET /api/auth/nickname/check?nickname={nickname}
     * 
     * 닉네임이 이미 사용 중인지 확인합니다.
     * 
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부 (true: 사용 가능, false: 중복)
     */
    @GetMapping("/nickname/check")
    public ResponseEntity<SuccessResponse<Boolean>> checkNickname(
            @RequestParam String nickname
    ) {
        // 1. 앱 서비스 호출 (닉네임 중복 확인)
        boolean isAvailable = authService.isNicknameAvailable(nickname);
        
        // 2. 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.",
                        isAvailable
                ));
    }
    
    /**
     * Google ID Token 로그인
     * 
     * POST /api/auth/google
     * 
     * 프론트엔드에서 Google Sign-In SDK를 통해 받은 ID Token으로 로그인합니다.
     * 신규 사용자인 경우 자동으로 회원가입 처리됩니다.
     * 
     * @param request Google ID Token을 포함한 요청
     * @return JWT Access Token과 Refresh Token
     * 
     * <h3>요청 예시</h3>
     * <pre>
     * POST /api/auth/google
     * Content-Type: application/json
     * 
     * {
     *   "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * </pre>
     * 
     * <h3>응답 예시</h3>
     * <pre>
     * {
     *   "code": "SUCCESS",
     *   "message": "Google 로그인 성공",
     *   "data": {
     *     "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
     *     "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *     "user": {
     *       "email": "user@gmail.com",
     *       "nickname": "user123",
     *       "name": "홍길동"
     *     }
     *   }
     * }
     * </pre>
     */
    @PostMapping("/google")
    public ResponseEntity<SuccessResponse<LoginResponse>> googleLogin(
            @Valid @RequestBody org.cmarket.cmarket.web.auth.dto.GoogleLoginRequest request
    ) {
        // 1. Google ID Token 검증 및 사용자 조회/생성
        org.cmarket.cmarket.domain.auth.model.User user = googleAuthService.authenticateWithIdToken(request.getIdToken());
        
        // 2. JWT 토큰 생성
        String role = user.getRole().name();
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), role);
        
        // 3. 응답 생성
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserWebDto.builder()
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .name(user.getName())
                        .build())
                .build();
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "Google 로그인 성공",
                        loginResponse
                ));
    }
}

