package org.cmarket.cmarket.domain.auth.app.service;

import org.cmarket.cmarket.domain.auth.app.dto.LoginCommand;
import org.cmarket.cmarket.domain.auth.app.dto.LoginResponse;
import org.cmarket.cmarket.domain.auth.app.dto.PasswordChangeCommand;
import org.cmarket.cmarket.domain.auth.app.dto.SignUpCommand;
import org.cmarket.cmarket.domain.auth.app.dto.UserDto;
import org.cmarket.cmarket.domain.auth.app.dto.WithdrawalCommand;

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
    
    /**
     * 로그아웃
     * 
     * 현재 사용 중인 JWT 토큰을 블랙리스트에 등록하여 무효화합니다.
     * 
     * @param token JWT 토큰
     * @param expiresAt 토큰 만료 시간
     */
    void logout(String token, java.time.LocalDateTime expiresAt);
    
    /**
     * 이메일 인증코드 발송
     * 
     * 회원가입 또는 비밀번호 재설정 시 사용되는 이메일 인증코드를 발송합니다.
     * 
     * @param email 사용자 이메일
     * @return 생성된 인증코드
     */
    String sendEmailVerificationCode(String email);
    
    /**
     * 비밀번호 재설정 인증코드 발송
     * 
     * 이메일로 사용자를 조회하고, 존재하면 인증코드를 발송합니다.
     * 
     * @param email 사용자 이메일
     * @throws IllegalArgumentException 사용자가 존재하지 않을 때
     */
    String sendPasswordResetCode(String email);
    
    /**
     * 비밀번호 재설정
     * 
     * 이메일 인증이 완료된 상태에서 비밀번호를 변경합니다.
     * 클라이언트에서 이미 인증코드 검증을 완료한 후 호출됩니다.
     * 
     * @param email 사용자 이메일
     * @param newPassword 새 비밀번호
     * @throws IllegalArgumentException 이메일 인증이 완료되지 않았거나 사용자가 존재하지 않을 때
     */
    void resetPassword(String email, String newPassword);
    
    /**
     * 비밀번호 변경
     * 
     * 로그인한 사용자가 현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다.
     * 
     * @param command 비밀번호 변경 명령 (email, currentPassword, newPassword 포함)
     * @throws IllegalArgumentException 현재 비밀번호가 일치하지 않거나 사용자가 존재하지 않을 때
     */
    void changePassword(PasswordChangeCommand command);
    
    /**
     * 회원 탈퇴
     * 
     * 사용자 계정을 소프트 삭제 처리합니다.
     * 탈퇴 사유를 저장하고 deletedAt을 설정합니다.
     * 
     * @param command 회원 탈퇴 명령 (email 포함)
     * @throws IllegalArgumentException 사용자가 존재하지 않거나 이미 탈퇴한 경우
     */
    void withdraw(WithdrawalCommand command);
    
    /**
     * 닉네임 중복 확인
     * 
     * 닉네임이 이미 사용 중인지 확인합니다.
     * 
     * @param nickname 확인할 닉네임
     * @return 사용 가능하면 true, 중복이면 false
     */
    boolean isNicknameAvailable(String nickname);

    /**
     * 이메일 중복 확인
     * 
     * 이메일이 이미 사용 중인지 확인합니다.
     *
     * @param email 확인할 이메일
     * @return 사용 가능하면 true, 중복이면 false
     */
    boolean isEmailAvailable(String email);
}

