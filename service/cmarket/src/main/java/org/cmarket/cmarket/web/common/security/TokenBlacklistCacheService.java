package org.cmarket.cmarket.web.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.auth.app.service.TokenBlacklistCache;
import org.cmarket.cmarket.domain.auth.repository.TokenBlacklistRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Redis 기반 토큰 블랙리스트 캐시 서비스
 *
 * JwtAuthenticationFilter에서 매 요청마다 DB 조회를 하면 커넥션 풀 고갈이 발생할 수 있어,
 * Redis를 사용하여 블랙리스트 조회 시 DB 부하를 제거합니다.
 *
 * - 로그아웃 시: Redis에 토큰 등록 (TTL = 토큰 만료 시점)
 * - 인증 시: Redis에서만 조회 (DB 조회 없음)
 * - Redis 장애 시: DB로 폴백
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistCacheService implements TokenBlacklistCache {

    private static final String KEY_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    public void addToBlacklist(String token, LocalDateTime expiresAt) {
        try {
            String key = buildKey(token);
            Duration ttl = Duration.between(LocalDateTime.now(), expiresAt);
            if (ttl.isNegative() || ttl.isZero()) {
                log.debug("토큰이 이미 만료됨, 블랙리스트 등록 생략: expiresAt={}", expiresAt);
                return;
            }
            stringRedisTemplate.opsForValue().set(key, "1", ttl);
            log.debug("토큰 블랙리스트 Redis 등록: key={}, ttlSeconds={}", key, ttl.getSeconds());
        } catch (Exception e) {
            log.warn("Redis 블랙리스트 등록 실패 (DB에는 저장됨): {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String key = buildKey(token);
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasKey)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("Redis 블랙리스트 조회 실패, DB 폴백: {}", e.getMessage());
            return tokenBlacklistRepository.existsByToken(token);
        }
    }

    /**
     * 토큰을 Redis 키로 변환 (해시 사용으로 키 길이 고정)
     */
    private String buildKey(String token) {
        return KEY_PREFIX + sha256Hex(token);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
