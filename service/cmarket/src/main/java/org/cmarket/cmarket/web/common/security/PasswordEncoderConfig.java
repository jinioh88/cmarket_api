package org.cmarket.cmarket.web.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 설정
 * 
 * BCryptPasswordEncoder를 빈으로 등록하여 비밀번호 암호화 및 검증에 사용합니다.
 * BCrypt는 단방향 해시 알고리즘으로, 암호화된 비밀번호는 복호화할 수 없습니다.
 * 로그인 시 입력된 비밀번호를 암호화하여 저장된 해시와 비교합니다.
 */
@Configuration
public class PasswordEncoderConfig {
    
    /**
     * BCryptPasswordEncoder 빈 등록
     * 
     * @return PasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

