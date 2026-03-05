package org.cmarket.cmarket.domain.auth.app.service;

import java.time.LocalDateTime;

/**
 * 토큰 블랙리스트 캐시 인터페이스
 *
 * 로그아웃된 JWT 토큰을 블랙리스트로 관리합니다.
 * Redis 등 빠른 저장소를 사용하여 매 요청마다의 DB 조회를 방지합니다.
 *
 * 구현체는 web 모듈에서 제공됩니다.
 */
public interface TokenBlacklistCache {

    /**
     * 토큰을 블랙리스트에 추가
     *
     * @param token    JWT 토큰
     * @param expiresAt 토큰 만료 시간 (이 시점에 Redis TTL로 자동 삭제)
     */
    void addToBlacklist(String token, LocalDateTime expiresAt);

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    boolean isBlacklisted(String token);
}
