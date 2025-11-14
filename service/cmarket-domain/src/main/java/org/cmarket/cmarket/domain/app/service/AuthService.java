package org.cmarket.cmarket.domain.app.service;

import org.cmarket.cmarket.domain.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.app.dto.LoginResponse;
import org.cmarket.cmarket.domain.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.app.dto.UserDto;

/**
 * 인증 서비스 인터페이스
 * 
 * 회원가입, 로그인 등 인증 관련 비즈니스 로직을 담당합니다.
 */
public interface AuthService {
    
    /**
     * 회원가입
     * 
     * @param command 회원가입 명령
     * @return 생성된 사용자 정보
     */
    UserDto signUp(SignUpCommand command);
    
    /**
     * 로그인
     * 
     * 이메일로 사용자를 조회하고 비밀번호를 검증합니다.
     * 실제 인증은 컨트롤러의 AuthenticationManager에서 처리됩니다.
     * 
     * @param command 로그인 명령
     * @return 로그인 응답 (사용자 정보)
     * @throws IllegalArgumentException 이메일/비밀번호 불일치 시
     */
    LoginResponse login(LoginCommand command);
}

