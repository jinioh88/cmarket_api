package org.cmarket.cmarket.web.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * OAuth2 사용자 DB 저장 서비스
 *
 * CustomOAuth2UserService에서 DB 작업만 분리하여 트랜잭션을 보장합니다.
 * 같은 클래스 내부 호출(self-invocation) 시 @Transactional이 적용되지 않는 문제를 해결합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserPersistenceService {

    private final UserRepository userRepository;

    /**
     * 소셜 사용자 조회/갱신/생성 (DB만 사용, 트랜잭션 범위 최소화)
     */
    @Transactional
    public User saveOrUpdateUser(AuthProvider provider, String email, String socialId, String name, String nickname) {
        Optional<User> existingUser = userRepository.findByProviderAndSocialId(provider, socialId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.isDeleted()) {
                user.restore();
                updateUserInfo(user, name, nickname);
                log.info("OAuth2 삭제된 계정 재가입: email={}, provider={}", email, provider);
            } else {
                updateUserInfo(user, name, nickname);
                log.info("OAuth2 기존 사용자 로그인: email={}, provider={}", email, provider);
            }
            return user;
        }
        return createNewUser(email, socialId, name, nickname, provider);
    }

    private void updateUserInfo(User user, String name, String nickname) {
        if (name != null && !name.isEmpty() && user.getName() == null) {
            user.updateName(name);
        }
        if (nickname != null && !nickname.isEmpty() && user.getNickname() == null) {
            user.updateNickname(nickname);
        }
        userRepository.saveAndFlush(user);
    }

    private User createNewUser(String email, String socialId, String name, String nickname, AuthProvider provider) {
        Optional<User> existingUserByEmail = userRepository.findByEmailAndDeletedAtIsNull(email);

        if (existingUserByEmail.isPresent()) {
            User existingUser = existingUserByEmail.get();
            existingUser.linkSocialAccount(provider, socialId);
            log.info("기존 계정에 소셜 로그인 연동: email={}, provider={}", email, provider);
            return userRepository.saveAndFlush(existingUser);
        }

        if (nickname == null || nickname.isEmpty()) {
            if (email.startsWith("kakao_") && email.endsWith("@kakao.local")) {
                nickname = "카카오" + socialId.substring(0, Math.min(6, socialId.length()));
            } else {
                nickname = email.split("@")[0];
            }
        }

        String finalNickname = nickname;
        int suffix = 1;
        while (userRepository.existsByNickname(finalNickname)) {
            finalNickname = nickname + suffix;
            suffix++;
        }

        User user = User.builder()
                .email(email)
                .password(null)
                .name(name != null ? name : "")
                .nickname(finalNickname)
                .birthDate(null)
                .addressSido(null)
                .addressGugun(null)
                .role(UserRole.USER)
                .provider(provider)
                .socialId(socialId)
                .build();

        log.info("소셜 로그인으로 신규 가입: email={}, provider={}", email, provider);
        return userRepository.saveAndFlush(user);
    }
}
