package org.cmarket.cmarket.domain.auth.app.service;

import org.cmarket.cmarket.domain.auth.app.dto.EmailVerificationSendCommand;
import org.cmarket.cmarket.domain.auth.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.auth.app.dto.LoginResponse;
import org.cmarket.cmarket.domain.auth.app.dto.PasswordChangeCommand;
import org.cmarket.cmarket.domain.auth.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.auth.app.dto.UserDto;
import org.cmarket.cmarket.domain.auth.app.dto.WithdrawalCommand;
import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.cmarket.cmarket.domain.auth.model.EmailVerification;
import org.cmarket.cmarket.domain.auth.model.TokenBlacklist;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.repository.EmailVerificationRepository;
import org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
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
    private final TokenBlacklistCache tokenBlacklistCache;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    
    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenBlacklistRepository tokenBlacklistRepository,
            TokenBlacklistCache tokenBlacklistCache,
            EmailVerificationService emailVerificationService,
            EmailVerificationRepository emailVerificationRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.tokenBlacklistCache = tokenBlacklistCache;
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
        
        // 4. 이메일 인증 완료 여부 확인
        //    화면이 막고 있을 뿐 서버는 안 막고 있었다. 인증 API를 아예 호출하지 않고
        //    가입만 해도 201이 떨어지는 것을 운영 서버에서 확인했다.
        //    resetPassword와 같은 방식이다 — 만료(isExpired)는 보지 않는다. 인증만 끝났으면
        //    폼을 늦게 채워도 가입할 수 있어야 한다(회원가입 폼은 입력 칸이 9개다).
        java.util.List<EmailVerification> verifications =
                emailVerificationRepository.findByEmail(command.getEmail());
        boolean isVerified = verifications.stream()
                .anyMatch(EmailVerification::isVerified);
        
        if (!isVerified) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다. 인증코드를 먼저 확인해주세요.");
        }
        
        // 5. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(command.getPassword());
        
        // 6. User 엔티티 생성 및 저장
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
        
        // 7. UserDto로 변환하여 반환
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
        
        // 2. TokenBlacklist 엔티티 생성 및 DB 저장 (감사/복구용)
        TokenBlacklist tokenBlacklist = TokenBlacklist.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
        
        tokenBlacklistRepository.save(tokenBlacklist);
        
        // 3. Redis 블랙리스트 등록 (매 요청 DB 조회 방지 → 커넥션 풀 고갈 완화)
        tokenBlacklistCache.addToBlacklist(token, expiresAt);
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
    
    /**
     * 계정 찾기 — 가입 방법을 메일로 안내한다 (#849)
     *
     * ⚠️ **바로 위 sendPasswordResetCode 와 일부러 다르게 짰다.** 그쪽은 없는 이메일이면
     *    예외를 던지고 소셜이면 어느 소셜인지 문구에 담는다 — 화면이 그것을 그대로 보여줘서
     *    남의 이메일을 넣어 본 사람도 알게 된다. 여기서는 그 길을 막는다.
     *
     *      없는 이메일   → 아무것도 안 한다 (예외도 안 던진다)
     *      있는 이메일   → 메일로만 알려준다. 메일함을 여는 사람은 그 주인뿐이다
     *
     *    그래서 호출한 쪽은 **회원이든 아니든 똑같은 200 과 똑같은 문구**를 돌려줄 수 있다.
     */
    @Override
    public void sendAccountMethodNotice(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email)
                .ifPresent(user -> emailService.sendAccountMethodNotice(email, user.getProvider()));
    }

    /**
     * 비밀번호 재설정 인증코드 발송 (#849 2단계 — 누구에게나 같은 답만 준다)
     *
     * ⚠️ **예전에는 갈라 말했다.** 없는 이메일이면 「등록되지 않은 이메일입니다」로 400 을,
     *    소셜이면 「카카오로 가입한 계정입니다」로 400 을 던졌다. 화면이 그것을 그대로
     *    보여줘서 **남의 이메일을 넣어 본 사람이 회원 여부와 가입 경로를 알아낼 수 있었다**
     *    (계정 열거). 그 정보는 피싱에 그대로 쓰인다.
     *
     *    이제 세 경우가 밖에서 구분되지 않는다.
     *
     *      없는 이메일   아무것도 안 한다
     *      소셜 계정     「가입 방법 안내」 메일을 보낸다 (계정 찾기와 같은 메일)
     *      LOCAL        인증코드 메일을 보낸다
     *      → 어느 쪽이든 예외를 안 던진다. 컨트롤러는 늘 같은 200 을 준다
     *
     * ⚠️ **친절을 잃지 않았다.** 예전에 화면이 하던 「카카오로 가입하셨어요」를 이제 **메일이**
     *    한다. 메일함을 여는 사람은 그 이메일의 주인뿐이라, 알아야 할 사람에게만 닿는다.
     *
     * ⚠️ **메일을 비동기로 보낸다.** 동기로 보내면 회원일 때만 SMTP 왕복만큼 느려져,
     *    같은 답만 줘도 **걸리는 시간으로** 회원 여부가 새어 나간다.
     *
     * ⚠️ **resetPassword() 는 그대로 둔다.** 거기는 인증코드를 통과한 사람만 오는 자리라
     *    갈라 말해도 안 샌다. 오히려 같은 말만 하게 하면 「왜 안 바뀌지」가 된다.
     */
    @Override
    public void sendPasswordResetCode(String email) {
        userRepository.findByEmailAndDeletedAtIsNull(email).ifPresent(user -> {
            if (user.getProvider() != AuthProvider.LOCAL) {
                // 소셜 계정 — 인증코드를 보내 봐야 쓸 데가 없다. 대신 가입 방법을 알려준다.
                emailService.sendAccountMethodNotice(email, user.getProvider());
                return;
            }

            EmailVerificationSendCommand command = EmailVerificationSendCommand.builder()
                    .email(email)
                    .build();
            String verificationCode = emailVerificationService.sendVerificationCode(command);
            emailService.sendPasswordResetCode(email, verificationCode);
        });
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        // 1. 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));
        
        // 2. 소셜 로그인 사용자는 비밀번호 재설정 불가
        //
        // ⚠️ 어느 소셜인지 문구에 담는다. 「소셜 로그인 사용자는…」으로 뭉뚱그리면 화면이
        //    「카카오·구글로 가입한 계정이에요」라고밖에 못 쓴다. 기억이 안 나서 여기까지 온
        //    사람에게 「아, 카카오로 했었지」를 되살려 주는 것이 그 화면의 일이다.
        if (user.getProvider() != AuthProvider.LOCAL) {
            String provider = user.getProvider().displayName();
            throw new IllegalArgumentException(provider + "로 가입한 계정입니다. " + provider + " 로그인을 이용해주세요.");
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
    public void changePassword(PasswordChangeCommand command) {
        // 1. 이메일로 사용자 조회 (소프트 삭제된 사용자 제외)
        User user = userRepository.findByEmailAndDeletedAtIsNull(command.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 2. 소셜 로그인 사용자는 비밀번호 변경 불가 (위와 같은 이유로 어느 소셜인지 담는다)
        if (user.getProvider() != AuthProvider.LOCAL) {
            String provider = user.getProvider().displayName();
            throw new IllegalArgumentException(provider + "로 가입한 계정입니다. 비밀번호가 없어 변경할 수 없어요.");
        }
        
        // 3. 현재 비밀번호 확인
        if (user.getPassword() == null || !passwordEncoder.matches(command.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 4. 비밀번호 암호화 및 변경
        String encodedPassword = passwordEncoder.encode(command.getNewPassword());
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
    
    @Override
    public boolean isEmailAvailable(String email) {
        // 이메일 중복 확인
        return !userRepository.existsByEmail(email);
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

