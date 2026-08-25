package org.cmarket.cmarket.web.auth.service;

import org.cmarket.cmarket.domain.auth.app.service.EmailService;
import org.cmarket.cmarket.domain.auth.model.AuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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
            message.setSubject("[Cuddle Market] 이메일 인증코드");
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
     * 비밀번호 재설정 인증코드 발송 (#849 2단계)
     *
     * ⚠️ **비동기다.** 동기로 보내면 회원일 때만 SMTP 왕복만큼 느려져, 화면 문구를 뭉개도
     *    **걸리는 시간으로** 회원 여부가 새어 나간다.
     *
     * ⚠️ 회원가입의 sendVerificationCode 는 **동기 그대로 둔다** — 그쪽은 메일이 안 나가면
     *    그 자리에서 알려야 한다.
     */
    @Override
    @Async("mailTaskExecutor")
    public void sendPasswordResetCode(String to, String verificationCode) {
        String emailContent = buildVerificationEmailContent(verificationCode);

        if (mailEnabled) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject("[Cuddle Market] 이메일 인증코드");
                message.setText(emailContent);

                mailSender.send(message);
                log.info("비밀번호 재설정 인증코드 발송 완료: {}", to);
            } catch (Exception e) {
                // 비동기라 여기서 던져도 호출한 쪽으로 안 간다. 이미 응답이 나갔다.
                log.warn("비밀번호 재설정 인증코드 발송 실패: {}", to, e);
            }
        } else {
            log.info("=== 비밀번호 재설정 인증코드 (개발 모드) ===");
            log.info("수신자: {}", to);
            log.info("인증코드: {}", verificationCode);
            log.info("================================");
        }
    }

    /**
     * 가입 방법 안내 메일 발송 (#849 계정 찾기)
     *
     * ⚠️ **반드시 비동기로 돌린다.** 메일 발송이 요청 스레드에 있으면 회원일 때만
     *    SMTP 왕복만큼 느려진다 — 화면 문구가 같아도 **걸리는 시간이 다르면** 그것으로
     *    회원 여부를 알아낼 수 있다(계정 열거). 풀은 메일 전용이다(MailAsyncConfig).
     *
     * ⚠️ 비동기라 여기서 던진 예외는 호출한 쪽으로 안 간다. 그래서 실패해도 로그만 남긴다 —
     *    화면은 이미 「가입된 계정이 있다면 안내 메일을 보냈습니다」를 돌려준 뒤다.
     */
    @Override
    @Async("mailTaskExecutor")
    public void sendAccountMethodNotice(String to, AuthProvider provider) {
        String emailContent = buildAccountMethodEmailContent(provider);

        if (mailEnabled) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject("[Cuddle Market] 로그인 방법 안내");
                message.setText(emailContent);

                mailSender.send(message);
                log.info("가입 방법 안내 메일 발송 완료: {}", to);
            } catch (Exception e) {
                // 여기서 멈추면 안 된다. 이미 응답은 나갔다.
                log.warn("가입 방법 안내 메일 발송 실패: {}", to, e);
            }
        } else {
            // 개발 환경: 실제 이메일 발송 없이 로그만 출력
            log.info("=== 가입 방법 안내 (개발 모드) ===");
            log.info("수신자: {}", to);
            log.info("본문:\n{}", emailContent);
            log.info("================================");
        }
    }

    /**
     * 가입 방법 안내 이메일 본문 생성
     *
     * 갈래는 둘이다 — 이메일로 가입한 사람에게는 비밀번호 찾기로, 소셜로 가입한 사람에게는
     * 그 소셜 로그인으로 안내한다.
     *
     * @param provider 가입 경로
     * @return 이메일 본문
     */
    private String buildAccountMethodEmailContent(AuthProvider provider) {
        String providerName = provider.displayName();
        String guide = provider == AuthProvider.LOCAL
                ? "비밀번호로 로그인해주세요. 비밀번호가 기억나지 않으시면 「비밀번호 찾기」를 이용해주세요."
                : providerName + " 로그인을 이용해주세요. 비밀번호는 따로 없습니다.";

        return String.format(
                """
                안녕하세요. Cuddle Market입니다.
                
                요청하신 계정의 가입 방법을 안내해 드립니다.
                
                커들마켓은 %s(으)로 가입되어 있어요.
                %s
                
                로그인: https://cuddle-market.vercel.app/auth/login
                (앱에서도 같은 방법으로 로그인하실 수 있어요.)
                
                본인이 요청한 것이 아니라면 이 이메일을 무시하셔도 됩니다.
                
                감사합니다.
                """,
                providerName,
                guide
        );
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
                안녕하세요. Cuddle Market입니다.
                
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
