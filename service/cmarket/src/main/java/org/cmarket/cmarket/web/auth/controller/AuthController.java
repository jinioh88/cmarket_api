package org.cmarket.cmarket.web.controller;

import org.cmarket.cmarket.domain.auth.app.dto.EmailVerificationVerifyCommand;
import org.cmarket.cmarket.domain.auth.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.auth.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.auth.app.dto.WithdrawalCommand;
import org.cmarket.cmarket.domain.auth.app.service.AuthService;
import org.cmarket.cmarket.domain.auth.app.service.EmailVerificationService;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.JwtTokenProvider;
import org.cmarket.cmarket.web.dto.EmailVerificationSendRequest;
import org.cmarket.cmarket.web.dto.EmailVerificationVerifyRequest;
import org.cmarket.cmarket.web.dto.LoginRequest;
import org.cmarket.cmarket.web.dto.PasswordResetRequest;
import org.cmarket.cmarket.web.dto.PasswordResetSendRequest;
import org.cmarket.cmarket.web.dto.SignUpRequest;
import org.cmarket.cmarket.web.dto.UserWebDto;
import org.cmarket.cmarket.web.dto.WithdrawalRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

/**
 * 인증 관련 컨트롤러
 * 
 * 회원가입, 로그인, 이메일 인증 등의 인증 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;  // 검증용으로만 사용
    
    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            EmailVerificationService emailVerificationService
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailVerificationService = emailVerificationService;
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
        authService.sendEmailVerificationCode(request.getEmail());
        
        // 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "인증 번호를 발송했습니다."
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
    public ResponseEntity<SuccessResponse<org.cmarket.cmarket.web.dto.LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
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
        org.cmarket.cmarket.web.dto.LoginResponse loginWebResponse = org.cmarket.cmarket.web.dto.LoginResponse.builder()
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
     * 비밀번호 재설정 인증코드 발송
     * 
     * 사용자가 비밀번호를 잊었을 때, 이메일로 인증코드를 발송합니다.
     * 
     * @param request 비밀번호 재설정 인증코드 발송 요청
     * @return 성공 응답
     */
    @PostMapping("/password/reset/send")
    public ResponseEntity<SuccessResponse<String>> sendPasswordResetCode(
            @Valid @RequestBody PasswordResetSendRequest request
    ) {
        // 1. 앱 서비스 호출 (이메일로 사용자 조회 및 인증코드 발송)
        authService.sendPasswordResetCode(request.getEmail());
        
        // 2. 응답 반환
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(
                        ResponseCode.SUCCESS,
                        "인증 번호를 발송했습니다."
                ));
    }
    
    /**
     * 비밀번호 재설정
     * 
     * 인증코드를 검증하고 비밀번호를 변경합니다.
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
}

