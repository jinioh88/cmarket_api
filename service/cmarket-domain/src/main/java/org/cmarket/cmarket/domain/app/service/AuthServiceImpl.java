package org.cmarket.cmarket.domain.app.service;

import org.cmarket.cmarket.domain.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.app.dto.LoginResponse;
import org.cmarket.cmarket.domain.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.app.dto.UserDto;
import org.cmarket.cmarket.domain.model.AuthProvider;
import org.cmarket.cmarket.domain.model.User;
import org.cmarket.cmarket.domain.model.UserRole;
import org.cmarket.cmarket.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    
    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

