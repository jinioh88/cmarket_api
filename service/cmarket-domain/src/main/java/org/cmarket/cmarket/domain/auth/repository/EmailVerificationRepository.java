package org.cmarket.cmarket.domain.repository;

import org.cmarket.cmarket.domain.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * EmailVerification 엔티티 레포지토리 인터페이스
 * 
 * 이메일 인증코드 관련 데이터 접근을 담당합니다.
 * 
 * 주요 기능:
 * - 이메일과 인증코드로 조회
 * - 만료된 인증코드 삭제
 */
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    /**
     * 이메일과 인증코드로 조회
     * 
     * @param email 이메일 주소
     * @param verificationCode 인증코드
     * @return EmailVerification (없으면 Optional.empty())
     */
    Optional<EmailVerification> findByEmailAndVerificationCode(String email, String verificationCode);
    
    /**
     * 만료된 인증코드 삭제
     * 
     * @param now 현재 시간 (이 시간 이전의 만료된 인증코드 삭제)
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
    
    /**
     * 이메일로 모든 인증코드 조회
     * 
     * @param email 이메일 주소
     * @return 인증코드 목록
     */
    java.util.List<EmailVerification> findByEmail(String email);
    
    /**
     * 이메일로 모든 인증코드 삭제
     * 
     * @param email 이메일 주소
     */
    void deleteByEmail(String email);
}

