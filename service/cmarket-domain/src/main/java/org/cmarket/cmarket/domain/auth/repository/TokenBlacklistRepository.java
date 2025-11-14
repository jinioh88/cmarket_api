package org.cmarket.cmarket.domain.auth.repository;

import org.cmarket.cmarket.domain.auth.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * TokenBlacklist 엔티티 레포지토리 인터페이스
 * 
 * 로그아웃된 토큰 블랙리스트 관련 데이터 접근을 담당합니다.
 * 
 * 주요 기능:
 * - 토큰 존재 확인 (블랙리스트에 있는지 확인)
 * - 만료된 토큰 삭제
 */
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    
    /**
     * 토큰 존재 확인 (블랙리스트에 있는지 확인)
     * 
     * @param token JWT 토큰 문자열
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    boolean existsByToken(String token);
    
    /**
     * 토큰으로 조회
     * 
     * @param token JWT 토큰 문자열
     * @return TokenBlacklist (없으면 Optional.empty())
     */
    Optional<TokenBlacklist> findByToken(String token);
    
    /**
     * 만료된 토큰 삭제
     * 
     * @param now 현재 시간 (이 시간 이전의 만료된 토큰 삭제)
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
}

