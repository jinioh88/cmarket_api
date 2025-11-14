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
        
        // 4. 이메일이 없으면 예외 발생 (구글은 항상 있지만, 카카오는 동의 필요)
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("이메일 정보가 필요합니다. 소셜 로그인 시 이메일 제공에 동의해주세요.");
        }
        
        // 5. 기존 사용자 조회 (소셜 ID로 조회)
        Optional<User> existingUser = userRepository.findByProviderAndSocialId(provider, socialId);
        
        User user;
        if (existingUser.isPresent()) {
            // 6-1. 기존 사용자: 정보 업데이트 (이름, 닉네임 등)
            user = existingUser.get();
            updateUserInfo(user, name, nickname);
        } else {
            // 6-2. 신규 사용자: 자동 회원가입
            user = createNewUser(email, socialId, name, nickname, provider);
        }
        
        // 7. PrincipalDetails 생성 및 반환
        return new PrincipalDetails(user, attributes);
    }
    
    /**
     * 이메일 추출
     */
    private String extractEmail(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            return (String) attributes.get("email");
        } else if (provider == AuthProvider.KAKAO) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                return (String) kakaoAccount.get("email");
            }
        }
        return null;
    }
    
    /**
     * 소셜 ID 추출
     */
    private String extractSocialId(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            return (String) attributes.get("sub");
        } else if (provider == AuthProvider.KAKAO) {
            return String.valueOf(attributes.get("id"));
        }
        return null;
    }
    
    /**
     * 이름 추출
     */
    private String extractName(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            return (String) attributes.get("name");
        } else if (provider == AuthProvider.KAKAO) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    return (String) profile.get("nickname");
                }
            }
        }
        return null;
    }
    
    /**
     * 닉네임 추출
     */
    private String extractNickname(Map<String, Object> attributes, AuthProvider provider) {
        if (provider == AuthProvider.GOOGLE) {
            return (String) attributes.get("name");
        } else if (provider == AuthProvider.KAKAO) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    return (String) profile.get("nickname");
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
     * 신규 사용자 생성
     */
    private User createNewUser(String email, String socialId, String name, String nickname, AuthProvider provider) {
        // 이메일 중복 확인 (다른 Provider로 가입한 경우)
        Optional<User> existingUserByEmail = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (existingUserByEmail.isPresent()) {
            throw new OAuth2AuthenticationException("이미 가입된 이메일입니다. 일반 로그인을 사용해주세요.");
        }
        
        // 닉네임이 없으면 이메일 앞부분 사용
        if (nickname == null || nickname.isEmpty()) {
            nickname = email.split("@")[0];
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
        
        return userRepository.save(user);
    }
}

