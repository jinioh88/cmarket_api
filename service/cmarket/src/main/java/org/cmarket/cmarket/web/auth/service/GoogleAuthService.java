package org.cmarket.cmarket.web.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.web.common.security.GoogleIdTokenVerifierService;
import org.cmarket.cmarket.web.common.security.GoogleIdTokenVerifierService.GoogleUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Google ID Token 기반 인증 서비스
 * 
 * 프론트엔드에서 Google Sign-In SDK를 통해 받은 ID Token을 검증하고,
 * 사용자를 조회하거나 신규 가입 처리를 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GoogleAuthService {
    
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final UserRepository userRepository;
    
    /**
     * Google ID Token을 검증하고 사용자를 반환합니다.
     * 신규 사용자인 경우 자동으로 회원가입 처리합니다.
     *
     * @param idToken Google ID Token
     * @return 인증된 사용자
     * @throws IllegalArgumentException ID Token이 유효하지 않은 경우
     */
    public User authenticateWithIdToken(String idToken) {
        // 1. ID Token 검증
        GoogleUserInfo userInfo = googleIdTokenVerifierService.verify(idToken);
        
        if (userInfo == null) {
            throw new IllegalArgumentException("유효하지 않은 Google ID Token입니다.");
        }
        
        // 2. 기존 사용자 조회 (소셜 ID로)
        Optional<User> existingUser = userRepository.findByProviderAndSocialId(
                AuthProvider.GOOGLE, 
                userInfo.socialId()
        );
        
        if (existingUser.isPresent()) {
            // 3-1. 기존 사용자: 정보 업데이트 후 반환
            User user = existingUser.get();
            updateUserInfo(user, userInfo);
            return user;
        } else {
            // 3-2. 신규 사용자: 자동 회원가입
            return createNewUser(userInfo);
        }
    }
    
    /**
     * 기존 사용자 정보 업데이트
     */
    private void updateUserInfo(User user, GoogleUserInfo userInfo) {
        // 이름이 없으면 업데이트
        if (user.getName() == null || user.getName().isEmpty()) {
            if (userInfo.name() != null && !userInfo.name().isEmpty()) {
                user.updateName(userInfo.name());
            }
        }
        userRepository.save(user);
    }
    
    /**
     * 신규 사용자 생성
     */
    private User createNewUser(GoogleUserInfo userInfo) {
        String email = userInfo.email();
        String socialId = userInfo.socialId();
        String name = userInfo.name();
        
        // 이메일 중복 확인 (다른 방식으로 가입한 경우)
        Optional<User> existingUserByEmail = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (existingUserByEmail.isPresent()) {
            User existingUser = existingUserByEmail.get();
            if (existingUser.getProvider() == AuthProvider.LOCAL) {
                throw new IllegalArgumentException("이미 일반 회원으로 가입된 이메일입니다. 일반 로그인을 사용해주세요.");
            } else {
                throw new IllegalArgumentException("이미 " + existingUser.getProvider() + " 계정으로 가입된 이메일입니다.");
            }
        }
        
        // 닉네임 생성 (이메일 앞부분 사용)
        String nickname = email.split("@")[0];
        
        // 닉네임 중복 확인 및 처리
        String finalNickname = nickname;
        int suffix = 1;
        while (userRepository.existsByNickname(finalNickname)) {
            finalNickname = nickname + suffix;
            suffix++;
        }
        
        // User 엔티티 생성
        User user = User.builder()
                .email(email)
                .password(null)  // 소셜 로그인은 비밀번호 없음
                .name(name != null ? name : "")
                .nickname(finalNickname)
                .birthDate(null)
                .addressSido(null)
                .addressGugun(null)
                .role(UserRole.USER)
                .provider(AuthProvider.GOOGLE)
                .socialId(socialId)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Google 신규 사용자 가입 완료: email={}, nickname={}", email, finalNickname);
        
        return savedUser;
    }
}
