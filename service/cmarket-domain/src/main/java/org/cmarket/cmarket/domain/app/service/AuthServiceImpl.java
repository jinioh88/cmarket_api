package org.cmarket.cmarket.domain.app.service;

import org.cmarket.cmarket.domain.app.dto.EmailVerificationSendCommand;
import org.cmarket.cmarket.domain.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.app.dto.LoginResponse;
import org.cmarket.cmarket.domain.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.app.dto.UserDto;
import org.cmarket.cmarket.domain.app.dto.WithdrawalCommand;
import org.cmarket.cmarket.domain.model.AuthProvider;
import org.cmarket.cmarket.domain.model.EmailVerification;
import org.cmarket.cmarket.domain.model.TokenBlacklist;
import org.cmarket.cmarket.domain.model.User;
import org.cmarket.cmarket.domain.model.UserRole;
import org.cmarket.cmarket.domain.repository.EmailVerificationRepository;
import org.cmarket.cmarket.domain.repository.TokenBlacklistRepository;
import org.cmarket.cmarket.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * 인증 서비스 구현체
 * 
 * 회원가입, 로그인 등 인증 관련 비즈니스 로직을 담당합니다.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private static final int MINIMUM_AGE = 14;  // 만 14세 이상
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    
    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenBlacklistRepository tokenBlacklistRepository,
            EmailVerificationService emailVerificationService,
            EmailVerificationRepository emailVerificationRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationRepository = emailVerificationRepository;
        this.emailService = emailService;
    }
    
    @Override
    public UserDto signUp(SignUpCommand command) {
        // 1. 이메일 중복 검증
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 2. 닉네임 중복 검증
        if (userRepository.existsByNickname(command.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        // 3. 만 14세 이상 검증
        validateAge(command.getBirthDate());
        
        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(command.getPassword());
        
        // 5. User 엔티티 생성 및 저장
        User user = User.builder()
                .email(command.getEmail())
                .password(encodedPassword)
                .name(command.getName())
                .nickname(command.getNickname())
                .birthDate(command.getBirthDate())
                .addressSido(command.getAddressSido())
                .addressGugun(command.getAddressGugun())
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .socialId(null)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // 6. UserDto로 변환하여 반환
        return UserDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .nickname(savedUser.getNickname())
                .birthDate(savedUser.getBirthDate())
                .addressSido(savedUser.getAddressSido())
                .addressGugun(savedUser.getAddressGugun())
                .build();
    }
    
    @Override
    public LoginResponse login(LoginCommand command) {
        // 1. 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
        User user = userRepository.findByEmailAndDeletedAtIsNull(command.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));
        
        // 2. 비밀번호 검증
        if (user.getPassword() == null || !passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        
        // 3. UserDto 생성
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .addressSido(user.getAddressSido())
                .addressGugun(user.getAddressGugun())
                .build();
        
        // 4. LoginResponse 반환 (토큰은 컨트롤러에서 추가)
        return LoginResponse.builder()
                .user(userDto)
                .build();
    }
    
    @Override
    public void logout(String token, LocalDateTime expiresAt) {
        // 1. 토큰이 이미 블랙리스트에 있는지 확인
        if (tokenBlacklistRepository.existsByToken(token)) {
            // 이미 블랙리스트에 있으면 중복 처리 방지
            return;
        }
        
        // 2. TokenBlacklist 엔티티 생성 및 저장
        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
        
        tokenBlacklistRepository.save(tokenBlacklist);
    }
    
    @Override
    public String sendEmailVerificationCode(String email) {
        // 1. 이메일 인증코드 생성 및 저장
        EmailVerificationSendCommand command = EmailVerificationSendCommand.builder()
                .email(email)
                .build();
        String verificationCode = emailVerificationService.sendVerificationCode(command);
        
        // 2. 이메일 발송
        emailService.sendVerificationCode(email, verificationCode);
        
        // 3. 인증코드 반환
        return verificationCode;
    }
    
    @Override
    public void sendPasswordResetCode(String email) {
        // 1. 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));
        
        // 2. 소셜 로그인 사용자는 비밀번호 재설정 불가
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호 재설정이 불가능합니다.");
        }
        
        // 3. 이메일 인증코드 발송
        EmailVerificationSendCommand command = EmailVerificationSendCommand.builder()
                .email(email)
                .build();
        emailVerificationService.sendVerificationCode(command);
    }
    
    @Override
    public void resetPassword(String email, String newPassword) {
        // 1. 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));
        
        // 2. 소셜 로그인 사용자는 비밀번호 재설정 불가
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호 재설정이 불가능합니다.");
        }
        
        // 3. 이메일 인증 완료 여부 확인 (클라이언트에서 이미 인증코드 검증 완료)
        java.util.List<EmailVerification> verifications = emailVerificationRepository.findByEmail(email);
        boolean isVerified = verifications.stream()
                .anyMatch(EmailVerification::isVerified);
        
        if (!isVerified) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다. 인증코드를 먼저 확인해주세요.");
        }
        
        // 4. 비밀번호 암호화 및 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);
        userRepository.save(user);
    }
    
    @Override
    public void withdraw(WithdrawalCommand command) {
        // 1. 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
        User user = userRepository.findByEmailAndDeletedAtIsNull(command.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 3. 진행 중인 거래 확인 (향후 구현 예정)
        // TODO: 거래 서비스 구현 후 진행 중인 거래 확인 로직 추가
        // if (hasActiveTransaction(user.getId())) {
        //     throw new IllegalArgumentException("진행 중인 거래가 있습니다. 거래를 완료한 후 탈퇴해주세요.");
        // }
        
        // 4. 탈퇴 사유 저장 및 소프트 삭제 처리
        user.softDelete(command.getReason(), command.getDetailReason());
        userRepository.save(user);
        
        // 5. 소셜 로그인인 경우 소셜 연결 끊기 (향후 구현 예정)
        // TODO: OAuth2 연결 해제 API 호출
        // if (user.getProvider() != AuthProvider.LOCAL) {
        //     disconnectSocialAccount(user.getProvider(), user.getSocialId());
        // }
        
        // 6. 관련 데이터 삭제 (게시글, 댓글 등 - 향후 구현 예정)
        // TODO: 게시글, 댓글 등 관련 데이터 삭제 로직 추가
        // deleteUserRelatedData(user.getId());
    }
    
    @Override
    public boolean isNicknameAvailable(String nickname) {
        // 닉네임 중복 확인 (소프트 삭제된 사용자 제외)
        return !userRepository.existsByNickname(nickname);
    }
    
    /**
     * 만 14세 이상 검증
     * 
     * @param birthDate 생년월일
     * @throws IllegalArgumentException 만 14세 미만일 때
     */
    private void validateAge(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();
        
        if (age < MINIMUM_AGE) {
            throw new IllegalArgumentException("만 14세 이상만 회원가입이 가능합니다.");
        }
    }
}

