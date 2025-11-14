package org.cmarket.cmarket.domain.auth.app.service;

import org.cmarket.cmarket.domain.auth.app.dto.EmailVerificationSendCommand;
import org.cmarket.cmarket.domain.auth.app.dto.EmailVerificationVerifyCommand;
import org.cmarket.cmarket.domain.auth.model.EmailVerification;
import org.cmarket.cmarket.domain.auth.repository.EmailVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 이메일 인증 서비스 구현체
 * 
 * 이메일 인증코드 발송 및 검증을 담당합니다.
 */
@Service
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;
    
    private final EmailVerificationRepository emailVerificationRepository;
    
    public EmailVerificationServiceImpl(EmailVerificationRepository emailVerificationRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
    }
    
    @Override
    public String sendVerificationCode(EmailVerificationSendCommand command) {
        String email = command.getEmail();
        
        // 6자리 랜덤 인증코드 생성
        String verificationCode = generateVerificationCode();
        
        // 만료 시간 계산 (현재 시간 + 5분)
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES);
        
        // 기존 인증코드가 있으면 삭제 (같은 이메일로 재요청 시)
        emailVerificationRepository.deleteByEmail(email);
        
        // 새 인증코드 저장
        EmailVerification emailVerification = EmailVerification.builder()
                .email(email)
                .verificationCode(verificationCode)
                .expiresAt(expiresAt)
                .build();
        
        emailVerificationRepository.save(emailVerification);

        return verificationCode;
    }
    
    @Override
    public boolean verifyCode(EmailVerificationVerifyCommand command) {
        String email = command.getEmail();
        String verificationCode = command.getVerificationCode();
        
        // 이메일과 인증코드로 조회
        EmailVerification emailVerification = emailVerificationRepository
                .findByEmailAndVerificationCode(email, verificationCode)
                .orElse(null);
        
        if (emailVerification == null) {
            return false;
        }
        
        // 만료 여부 확인
        if (emailVerification.isExpired()) {
            return false;
        }
        
        // 이미 인증된 코드인지 확인
        if (emailVerification.isVerified()) {
            return false;
        }
        
        // 인증 완료 처리
        emailVerification.verify();
        emailVerificationRepository.save(emailVerification);
        
        return true;
    }
    
    /**
     * 6자리 랜덤 인증코드 생성
     * 
     * @return 6자리 숫자 문자열
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;  // 100000 ~ 999999
        return String.valueOf(code);
    }
}

