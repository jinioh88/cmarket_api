package org.cmarket.cmarket.domain.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이메일 인증코드 엔티티
 * 
 * 회원가입 및 비밀번호 재설정 시 사용되는 이메일 인증코드를 저장합니다.
 * - 인증코드는 6자리 숫자
 * - 만료 시간: 5분
 * - 인증 완료 시 verifiedAt에 시간 기록
 */
@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 6)
    private String verificationCode;  // 6자리 숫자
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;  // 만료 시간 (생성 시간 + 5분)
    
    @Column
    private LocalDateTime verifiedAt;  // 인증 완료 시간 (null이면 미인증)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    public EmailVerification(
            String email,
            String verificationCode,
            LocalDateTime expiresAt
    ) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * 인증코드 검증 완료 처리
     */
    public void verify() {
        this.verifiedAt = LocalDateTime.now();
    }
    
    /**
     * 인증 여부 확인
     */
    public boolean isVerified() {
        return this.verifiedAt != null;
    }
    
    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    /**
     * 유효한 인증코드인지 확인 (만료되지 않았고 아직 인증되지 않음)
     */
    public boolean isValid() {
        return !isExpired() && !isVerified();
    }
}

