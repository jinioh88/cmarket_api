package org.cmarket.cmarket.web.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

/**
 * 횟수 제한 (#849)
 *
 * 계정 열거를 화면 문구로 막아 놔도, **수만 번 두드리면** 다른 신호(응답 시간·메일 반송 등)로
 * 새어 나갈 수 있다. 그래서 「한 사람이 짧은 시간에 얼마나 많이 물어볼 수 있는가」를 막는다.
 *
 * ⚠️ **Redis 를 쓴다.** 이미 토큰 블랙리스트가 같은 방식으로 쓰고 있어(TokenBlacklistCacheService)
 *    새 라이브러리를 안 들인다. INCR 로 세고 첫 번째일 때만 TTL 을 건다.
 *
 * ⚠️ **Redis 가 죽으면 막지 않는다(fail-open).** 횟수 제한은 서비스를 지키는 장치이지
 *    서비스 그 자체가 아니다. 여기서 막아 버리면 Redis 장애가 곧 로그인 불가가 된다.
 *    블랙리스트가 Redis 장애 때 DB 로 물러서는 것과 같은 결이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiter {

    private static final String KEY_PREFIX = "auth:ratelimit:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 한 번 쓴다. 한도를 넘었으면 false.
     *
     * @param bucket 무엇에 대한 제한인지 (예: "account-find:ip")
     * @param id     누구에 대한 것인지 (IP 또는 이메일)
     * @param limit  창 안에서 허용할 횟수
     * @param window 창 길이
     * @return 써도 되면 true, 한도를 넘었으면 false
     */
    /**
     * IP 기준으로 한 번 쓴다.
     *
     * ⚠️ **IP 가 구분되지 않으면 아예 세지 않는다.** 이 서비스는 앞에 nginx 가 있어서,
     *    nginx 가 X-Forwarded-For 를 안 넘기면 **모든 사용자가 127.0.0.1 로 보인다.**
     *    그대로 세면 한 사람이 아니라 **전체가 한꺼번에 막힌다** — 로그인 불가다.
     *    그래서 루프백이면 경고만 남기고 통과시킨다. 이메일 기준 제한은 그대로 살아 있다.
     */
    public boolean tryConsumeIp(String bucket, String ip, int limit, Duration window) {
        if (ip == null || ip.startsWith("127.") || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            log.warn("IP 가 구분되지 않아 횟수 제한을 건너뛴다(nginx 가 X-Forwarded-For 를 넘기는지 볼 것): ip={}", ip);
            return true;
        }
        return tryConsume(bucket, ip, limit, window);
    }

    /**
     * 이메일 기준으로 한 번 쓴다.
     *
     * ⚠️ **대문자와 앞뒤 빈칸을 지운 뒤 센다**(#1091). Redis 키는 넘어온 글자를 그대로
     *    쓰는데 **DB 는 대소문자를 안 가린다**(연결 collation 이 utf8mb4_general_ci 다).
     *    그래서 `A@x.com` 과 `a@x.com` 은 **같은 사람을 찾아 메일을 보내면서
     *    계수기는 따로 센다** — 첫 글자만 대문자로 바꾸면 창이 그대로 뚫린다.
     *    폰 자판이 첫 글자를 저절로 크게 만드는 일이 흔해서 **공격이 아니어도 걸린다.**
     *
     * ⚠️ 대소문자를 가리는 DB 였더라도 **이 고침은 해롭지 않다.** 그때는 대문자가 섞인
     *    이메일이 애초에 사람을 못 찾아 메일이 안 나가므로, 키를 맞춰도 달라질 것이 없다.
     */
    public boolean tryConsumeEmail(String bucket, String email, int limit, Duration window) {
        if (email == null) {
            return true;
        }
        return tryConsume(bucket, email.trim().toLowerCase(Locale.ROOT), limit, window);
    }

    public boolean tryConsume(String bucket, String id, int limit, Duration window) {
        String key = KEY_PREFIX + bucket + ":" + id;
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count == null) {
                return true;
            }
            if (count == 1L) {
                // 첫 번째일 때만 창을 연다. 매번 걸면 창이 계속 밀려 영원히 안 닫힌다.
                //
                // ⚠️ **알고도 안 고친 구멍이 하나 있다**(#1091 에서 짚었다). INCR 은 됐는데
                //    바로 이 expire 가 예외를 던지면, 아래 catch 가 그 요청은 통과시키지만
                //    **키는 TTL 없이 남는다.** 그러면 다음 요청부터 count 가 2·3·4… 로 늘어
                //    한도에 걸린다. **한도가 1 인 account-notice:sent 는 두 번째 요청부터
                //    영영 막힌다 — 10분이 지나도 안 풀린다.**
                //
                //    안 고친 까닭: INCR 과 expire 사이에 레디스 연결이 끊길 때뿐이라 아주 드물고,
                //    고치는 방법(「TTL 이 없으면 건다」로 바꾸기)은 왕복이 하나 늘면서도
                //    원자성은 여전히 불완전하다. 값어치보다 무게가 크다고 봤다.
                //
                //    ⚠️ **대신 이 글을 남긴다.** 「나만 안내 메일이 안 온다」는 신고가 들어오면
                //    이것부터 의심해라. 그 키의 TTL 을 보고, -1(창이 없음)이면 지우면 풀린다.
                //
                //      redis-cli TTL auth:ratelimit:account-notice:sent:<소문자 이메일>
                //      redis-cli DEL auth:ratelimit:account-notice:sent:<소문자 이메일>
                stringRedisTemplate.expire(key, window);
            }
            if (count > limit) {
                log.info("횟수 제한에 걸렸다: bucket={}, count={}, limit={}", bucket, count, limit);
                return false;
            }
            return true;
        } catch (Exception e) {
            // ⚠️ 막지 않는다. 위 주석 참고.
            log.warn("횟수 제한 확인 실패 — 통과시킨다: bucket={}, {}", bucket, e.getMessage());
            return true;
        }
    }
}
