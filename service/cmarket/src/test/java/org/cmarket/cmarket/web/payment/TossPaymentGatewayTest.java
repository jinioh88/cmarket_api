package org.cmarket.cmarket.web.payment;

import org.cmarket.cmarket.domain.payment.app.PaymentGatewayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * 최종 검토 I2: 같은 주문을 다시 확정하면 토스가 같은 승인으로 알아보게 Idempotency-Key 를 주문마다 고정한다.
 * 연결이 끊긴 경우는 「토스가 거절했다」와 구별한다 — 실제로는 승인됐을 수 있어 주문을 실패로 닫으면 안 된다.
 */
class TossPaymentGatewayTest {

    private static final String URL = "https://api.tosspayments.com/v1/payments/confirm";

    RestTemplate restTemplate;
    MockRestServiceServer server;
    TossPaymentGateway gateway;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        gateway = new TossPaymentGateway(new TossKeyGuard("test_sk_abc"), restTemplate);
    }

    @Test
    void confirm_usesOrderBasedIdempotencyKey_sameOnRetry() {
        for (int i = 0; i < 2; i++) {
            server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
                    .andExpect(header("Idempotency-Key", "confirm-CM1"))
                    .andRespond(withSuccess("{\"approvedAt\":\"2026-11-01T15:00:00+09:00\"}", MediaType.APPLICATION_JSON));
        }

        assertThat(gateway.confirm("pk_1", "CM1", 50000)).isEqualTo(LocalDateTime.of(2026, 11, 1, 15, 0));
        gateway.confirm("pk_1", "CM1", 50000);
        server.verify();
    }

    @Test
    void confirm_tossRejects_notRetryable() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"REJECT_CARD_COMPANY\",\"message\":\"카드사에서 거절했습니다.\"}"));

        assertThatThrownBy(() -> gateway.confirm("pk_1", "CM1", 50000))
                .isInstanceOfSatisfying(PaymentGatewayException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("REJECT_CARD_COMPANY");
                    assertThat(e.isRetryable()).isFalse();
                });
    }

    @Test
    void confirm_networkError_retryable() {
        server.expect(requestTo(URL)).andRespond(request -> {
            throw new ResourceAccessException("Read timed out");
        });

        assertThatThrownBy(() -> gateway.confirm("pk_1", "CM1", 50000))
                .isInstanceOfSatisfying(PaymentGatewayException.class, e -> assertThat(e.isRetryable()).isTrue());
    }

    @Test
    void confirm_tossServerError_retryable() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON).body("{\"code\":\"FAILED_INTERNAL_SYSTEM_PROCESSING\",\"message\":\"x\"}"));

        assertThatThrownBy(() -> gateway.confirm("pk_1", "CM1", 50000))
                .isInstanceOfSatisfying(PaymentGatewayException.class, e -> assertThat(e.isRetryable()).isTrue());
    }
}
