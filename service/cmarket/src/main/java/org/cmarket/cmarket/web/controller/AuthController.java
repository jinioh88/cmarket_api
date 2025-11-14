package org.cmarket.cmarket.web.controller;

import org.cmarket.cmarket.domain.app.dto.EmailVerificationSendCommand;
import org.cmarket.cmarket.domain.app.dto.EmailVerificationVerifyCommand;
import org.cmarket.cmarket.domain.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.app.service.AuthService;
import org.cmarket.cmarket.domain.app.service.EmailVerificationService;
import org.cmarket.cmarket.web.dto.EmailVerificationSendRequest;
import org.cmarket.cmarket.web.dto.EmailVerificationVerifyRequest;
import org.cmarket.cmarket.web.dto.LoginRequest;
import org.cmarket.cmarket.web.dto.LoginResponse;
import org.cmarket.cmarket.web.dto.SignUpRequest;
import org.cmarket.cmarket.web.dto.UserWebDto;
import org.cmarket.cmarket.web.response.ResponseCode;
import org.cmarket.cmarket.web.response.SuccessResponse;
import org.cmarket.cmarket.web.security.JwtTokenProvider;
import org.cmarket.cmarket.web.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AuthController(
            EmailVerificationService emailVerificationService,
            EmailService emailService,
            AuthService authService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
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
        // 웹 DTO → 앱 DTO 변환
        EmailVerificationSendCommand command = EmailVerificationSendCommand.builder()
                .email(request.getEmail())
                .build();
        
        // 인증코드 생성 및 저장
        String verificationCode = emailVerificationService.sendVerificationCode(command);
        
        // 이메일 발송
        emailService.sendVerificationCode(request.getEmail(), verificationCode);
        
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
        org.cmarket.cmarket.domain.app.dto.UserDto userDto = authService.signUp(command);
        
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
        org.cmarket.cmarket.domain.app.dto.LoginResponse loginResponse = authService.login(command);
        
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
}

