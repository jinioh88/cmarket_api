package org.cmarket.cmarket.web.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * 커스텀 OAuth2 사용자 서비스
 * 
 * 구글/카카오로부터 사용자 정보를 받아온 직후 호출됩니다.
 * 받아온 정보로 User를 조회하거나 생성하여 PrincipalDetails를 반환합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2UserService를 사용하여 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 2. Provider 정보 추출 (google, kakao)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
        
        // 3. 사용자 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes, provider);
        String socialId = extractSocialId(attributes, provider);
        String name = extractName(attributes, provider);
        String nickname = extractNickname(attributes, provider);
        
        // 4. 소셜 ID가 없으면 예외 발생
        if (socialId == null || socialId.isEmpty()) {
            log.error("소셜 ID 추출 실패: provider={}, attributes={}", provider, attributes);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("social_id_required", "소셜 로그인 정보를 가져오는데 실패했습니다. 다시 시도해주세요.", null)
            );
        }
        
        // 5. 이메일이 없으면 임시 이메일 생성 (카카오 일반 웹페이지 로그인은 이메일을 받을 수 없음)
        if (email == null || email.isEmpty()) {
            if (provider == AuthProvider.KAKAO) {
                // 카카오의 경우 이메일이 없으면 임시 이메일 생성
                email = "kakao_" + socialId + "@kakao.local";
                log.info("카카오 로그인: 이메일이 제공되지 않아 임시 이메일 생성. socialId={}, email={}", socialId, email);
            } else {
                // 구글은 항상 이메일이 있어야 함
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_required", "이메일 정보가 필요합니다. 소셜 로그인 시 이메일 제공에 동의해주세요.", null)
                );
            }
        }
        
        // 6. 기존 사용자 조회 (소셜 ID로 조회)
        Optional<User> existingUser = userRepository.findByProviderAndSocialId(provider, socialId);
        
        User user;
        if (existingUser.isPresent()) {
            // 7-1. 기존 사용자: 정보 업데이트 (이름, 닉네임 등)
            user = existingUser.get();
            updateUserInfo(user, name, nickname);
            log.info("OAuth2 기존 사용자 로그인: email={}, provider={}", email, provider);
        } else {
            // 7-2. 신규 사용자: 자동 회원가입
            user = createNewUser(email, socialId, name, nickname, provider);
        }
        
        // 8. PrincipalDetails 생성 및 반환
        return new PrincipalDetails(user, attributes);
    }
    
    /**
     * 이메일 추출
     * 
     * 카카오의 경우 kakao_account.email에서 추출합니다.
     * 카카오는 이메일 제공에 대한 별도 동의가 필요하므로 null일 수 있습니다.
     */
    private String extractEmail(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            Object email = attributes.get("email");
            return email != null ? String.valueOf(email) : null;
        } else if (provider == AuthProvider.KAKAO) {
            Object kakaoAccountObj = attributes.get("kakao_account");
            if (kakaoAccountObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
                Object email = kakaoAccount.get("email");
                return email != null ? String.valueOf(email) : null;
            }
        }
        return null;
    }
    
    /**
     * 소셜 ID 추출
     * 
     * 구글의 경우 "sub" 필드에서, 카카오의 경우 "id" 필드에서 추출합니다.
     * 카카오의 id는 Long 타입일 수 있으므로 String으로 변환합니다.
     */
    private String extractSocialId(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            Object sub = attributes.get("sub");
            return sub != null ? String.valueOf(sub) : null;
        } else if (provider == AuthProvider.KAKAO) {
            Object id = attributes.get("id");
            return id != null ? String.valueOf(id) : null;
        }
        return null;
    }
    
    /**
     * 이름 추출
     * 
     * 구글의 경우 "name" 필드에서, 카카오의 경우 kakao_account.profile.nickname에서 추출합니다.
     */
    private String extractName(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            Object name = attributes.get("name");
            return name != null ? String.valueOf(name) : null;
        } else if (provider == AuthProvider.KAKAO) {
            Object kakaoAccountObj = attributes.get("kakao_account");
            if (kakaoAccountObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
                Object profileObj = kakaoAccount.get("profile");
                if (profileObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> profile = (Map<String, Object>) profileObj;
                    Object nickname = profile.get("nickname");
                    return nickname != null ? String.valueOf(nickname) : null;
                }
            }
        }
        return null;
    }
    
    /**
     * 닉네임 추출
     * 
     * 구글의 경우 "name" 필드를 닉네임으로 사용하고,
     * 카카오의 경우 kakao_account.profile.nickname에서 추출합니다.
     */
    private String extractNickname(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            Object name = attributes.get("name");
            return name != null ? String.valueOf(name) : null;
        } else if (provider == AuthProvider.KAKAO) {
            Object kakaoAccountObj = attributes.get("kakao_account");
            if (kakaoAccountObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
                Object profileObj = kakaoAccount.get("profile");
                if (profileObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> profile = (Map<String, Object>) profileObj;
                    Object nickname = profile.get("nickname");
                    return nickname != null ? String.valueOf(nickname) : null;
                }
            }
        }
        return null;
    }
    
    /**
     * 기존 사용자 정보 업데이트
     */
    private void updateUserInfo(User user, String name, String nickname) {
        if (name != null && !name.isEmpty() && user.getName() == null) {
            user.updateName(name);
        }
        if (nickname != null && !nickname.isEmpty() && user.getNickname() == null) {
            user.updateNickname(nickname);
        }
        userRepository.save(user);
    }
    
    /**
     * 신규 사용자 생성 또는 기존 계정에 소셜 연동
     * 
     * 동일한 이메일로 이미 가입된 계정이 있으면 소셜 계정을 연동하고,
     * 없으면 새로운 사용자를 생성합니다.
     */
    private User createNewUser(String email, String socialId, String name, String nickname, AuthProvider provider) {
        // 이메일로 기존 사용자 조회
        Optional<User> existingUserByEmail = userRepository.findByEmailAndDeletedAtIsNull(email);
        
        if (existingUserByEmail.isPresent()) {
            // 기존 계정이 있으면 소셜 계정 연동
            User existingUser = existingUserByEmail.get();
            existingUser.linkSocialAccount(provider, socialId);
            log.info("기존 계정에 소셜 로그인 연동: email={}, provider={}", email, provider);
            return userRepository.save(existingUser);
        }
        
        // 닉네임이 없으면 생성
        if (nickname == null || nickname.isEmpty()) {
            // 임시 이메일인 경우 (kakao_xxx@kakao.local) 소셜 ID 기반으로 생성
            if (email.startsWith("kakao_") && email.endsWith("@kakao.local")) {
                nickname = "카카오" + socialId.substring(0, Math.min(6, socialId.length()));
            } else {
                nickname = email.split("@")[0];
            }
        }
        
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
                .birthDate(null)  // 소셜 로그인은 생년월일 정보 없음
                .addressSido(null)
                .addressGugun(null)
                .role(UserRole.USER)
                .provider(provider)
                .socialId(socialId)
                .build();
        
        log.info("소셜 로그인으로 신규 가입: email={}, provider={}", email, provider);
        return userRepository.save(user);
    }
}

