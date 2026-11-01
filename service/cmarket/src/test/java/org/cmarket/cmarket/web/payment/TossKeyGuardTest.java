package org.cmarket.cmarket.web.payment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트 비밀 키만 받는다. 라이브 키나 위젯 키가 들어오면 결제 주소를 닫는다 (#1126, 설계 8절)
 */
class TossKeyGuardTest {

    @Test void testSecretKey_usable()  { assertThat(new TossKeyGuard("test_sk_abc").isUsable()).isTrue(); }
    @Test void liveKey_notUsable()     { assertThat(new TossKeyGuard("live_sk_abc").isUsable()).isFalse(); }
    @Test void widgetKey_notUsable()   { assertThat(new TossKeyGuard("test_gsk_abc").isUsable()).isFalse(); }
    @Test void emptyKey_notUsable()    { assertThat(new TossKeyGuard("").isUsable()).isFalse(); }
    @Test void nullKey_notUsable()     { assertThat(new TossKeyGuard(null).isUsable()).isFalse(); }
}
