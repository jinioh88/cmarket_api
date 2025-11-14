package org.cmarket.cmarket.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 발송 서비스 구현체
 * 
 * Spring Mail을 사용하여 SMTP를 통해 이메일을 발송합니다.
 * 
 * 개발 환경에서는 spring.mail.host가 설정되지 않으면 실제 이메일 발송 없이 로그만 출력합니다.
 */
@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private JavaMailSender mailSender;
    private final String fromEmail;
    private final boolean mailEnabled;
    
    public EmailServiceImpl(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        // spring.mail.host가 설정되어 있고 JavaMailSender가 있으면 실제 이메일 발송 활성화
        this.mailEnabled = (mailSender != null) && !fromEmail.isEmpty();
    }
    
    @Override
    public void sendVerificationCode(String to, String verificationCode) {
        String emailContent = buildVerificationEmailContent(verificationCode);
        
        if (mailEnabled) {
            // 실제 이메일 발송
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("[Cmarket] 이메일 인증코드");
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("이메일 인증코드 발송 완료: {}", to);
        } else {
            // 개발 환경: 실제 이메일 발송 없이 로그만 출력
            log.info("=== 이메일 인증코드 (개발 모드) ===");
            log.info("수신자: {}", to);
            log.info("인증코드: {}", verificationCode);
            log.info("본문:\n{}", emailContent);
            log.info("================================");
        }
    }
    
    /**
     * 인증코드 이메일 본문 생성
     * 
     * @param verificationCode 인증코드
     * @return 이메일 본문
     */
    private String buildVerificationEmailContent(String verificationCode) {
        return String.format(
                """
                안녕하세요. Cmarket입니다.
                
                요청하신 이메일 인증코드입니다.
                
                인증코드: %s
                
                인증코드는 5분간 유효합니다.
                
                본인이 요청한 것이 아니라면 이 이메일을 무시하셔도 됩니다.
                
                감사합니다.
                """,
                verificationCode
        );
    }
}
