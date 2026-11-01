package org.cmarket.cmarket.web.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 토스페이먼츠 비밀 키를 들고 있다가, 테스트 비밀 키일 때만 결제를 연다 (#1126, 설계 8절)
 *
 * 이번 결제는 테스트 모드뿐이다. `live_` 키나 결제위젯 키(`test_gsk_`)가 들어오면 실제 돈이 오가거나
 * 승인 방식이 달라지므로, 결제 주소가 503 을 돌려주게 한다. 키는 어디에도 찍지 않는다.
 */
@Component
public class TossKeyGuard {

    private static final String TEST_SECRET_PREFIX = "test_sk_";

    private final String secretKey;

    public TossKeyGuard(@Value("${toss.secret-key:}") String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isUsable() {
        return secretKey != null && secretKey.startsWith(TEST_SECRET_PREFIX);
    }

    String secretKey() {
        return secretKey;
    }
}
