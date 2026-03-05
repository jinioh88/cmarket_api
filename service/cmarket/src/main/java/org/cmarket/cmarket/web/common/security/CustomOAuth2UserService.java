package org.cmarket.cmarket.web.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.cmarket.cmarket.domain.auth.model.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 커스텀 OAuth2 사용자 서비스
 *
 * 구글/카카오로부터 사용자 정보를 받아온 직후 호출됩니다.
 * 받아온 정보로 User를 조회하거나 생성하여 PrincipalDetails를 반환합니다.
 *
 * 트랜잭션: loadUser() 내부의 super.loadUser()가 Google/Kakao 사용자 정보 API(HTTP)를 호출하므로,
 * 트랜잭션을 걸면 DB 커넥션을 HTTP 대기 시간만큼 붙잡아 풀 고갈·504 유발.
 * DB 접근은 OAuth2UserPersistenceService에 위임 (별도 @Transactional 적용).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserPersistenceService oauth2UserPersistenceService;

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
                email = "kakao_" + socialId + "@kakao.local";
                log.info("카카오 로그인: 이메일이 제공되지 않아 임시 이메일 생성. socialId={}, email={}", socialId, email);
            } else {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_required", "이메일 정보가 필요합니다. 소셜 로그인 시 이메일 제공에 동의해주세요.", null)
                );
            }
        }

        // 6~7. DB 작업만 별도 서비스(트랜잭션)로 수행
        User user = oauth2UserPersistenceService.saveOrUpdateUser(provider, email, socialId, name, nickname);

        // 8. PrincipalDetails 생성 및 반환
        return new PrincipalDetails(user, attributes);
    }

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
}
