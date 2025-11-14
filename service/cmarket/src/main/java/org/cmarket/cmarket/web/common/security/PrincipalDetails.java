package org.cmarket.cmarket.web.security;

import lombok.Getter;
import org.cmarket.cmarket.domain.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 커스텀 UserDetails 구현체
 * 
 * Spring Security의 UserDetails와 OAuth2User를 모두 구현하여
 * 일반 로그인과 소셜 로그인을 모두 지원합니다.
 */
@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {
    
    private final User user;
    private final Map<String, Object> attributes;
    private final boolean isOAuth2User;
    
    /**
     * 일반 로그인용 생성자
     * 
     * @param user 사용자 엔티티
     */
    public PrincipalDetails(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
        this.isOAuth2User = false;
    }
    
    /**
     * OAuth2 로그인용 생성자
     * 
     * @param user 사용자 엔티티
     * @param attributes OAuth2 사용자 속성
     */
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
        this.isOAuth2User = true;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getDeletedAt() == null;
    }
    
    // OAuth2User 인터페이스 구현
    @Override
    public String getName() {
        return user.getEmail();
    }
}

