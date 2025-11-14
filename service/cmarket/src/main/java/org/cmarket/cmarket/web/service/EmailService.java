package org.cmarket.cmarket.web.service;

/**
 * 이메일 발송 서비스 인터페이스
 * 
 * 주요 기능:
 * - 인증코드 발송: 회원가입 및 비밀번호 재설정 시 사용
 */
public interface EmailService {
    
    /**
     * 이메일 인증코드 발송
     * 
     * @param to 수신자 이메일 주소
     * @param verificationCode 인증코드 (6자리 숫자)
     */
    void sendVerificationCode(String to, String verificationCode);
}

