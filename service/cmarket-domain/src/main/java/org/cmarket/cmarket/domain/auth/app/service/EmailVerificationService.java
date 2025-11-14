package org.cmarket.cmarket.domain.auth.app.service;

import org.cmarket.cmarket.domain.auth.app.dto.EmailVerificationSendCommand;
import org.cmarket.cmarket.domain.auth.app.dto.EmailVerificationVerifyCommand;

/**
 * 이메일 인증 서비스 인터페이스
 * 
 * 이메일 인증코드 발송 및 검증을 담당합니다.
 */
public interface EmailVerificationService {
    
    /**
     * 이메일 인증코드 발송
     * 
     * @param command 이메일 인증코드 발송 명령
     * @return 생성된 인증코드 (이메일 발송용)
     */
    String sendVerificationCode(EmailVerificationSendCommand command);
    
    /**
     * 이메일 인증코드 검증
     * 
     * @param command 이메일 인증코드 검증 명령
     * @return 검증 성공 여부
     */
    boolean verifyCode(EmailVerificationVerifyCommand command);
}

