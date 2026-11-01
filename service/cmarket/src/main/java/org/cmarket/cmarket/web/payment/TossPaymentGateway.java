package org.cmarket.cmarket.web.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.payment.app.PaymentGateway;
import org.cmarket.cmarket.domain.payment.app.PaymentGatewayException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;

/**
 * 토스 결제 승인 API 를 부른다 (#1126)
 *
 * POST https://api.tosspayments.com/v1/payments/confirm
 * 인증은 비밀 키 Basic(base64("키:")), 같은 요청이 두 번 승인되지 않게 Idempotency-Key 를 붙인다.
 * 키는 로그에 찍지 않는다.
 */
@Slf4j
@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    // 토스 권장 읽기 시간 초과가 길다(승인은 카드사까지 다녀온다). 그래도 끝없이 기다리며 주문 행을 잠그지 않게 한다
    private static final int READ_TIMEOUT_MS = 30_000;

    private final TossKeyGuard keyGuard;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Autowired
    public TossPaymentGateway(TossKeyGuard keyGuard) {
        this(keyGuard, defaultRestTemplate());
    }

    TossPaymentGateway(TossKeyGuard keyGuard, RestTemplate restTemplate) {
        this.keyGuard = keyGuard;
        this.restTemplate = restTemplate;
    }

    private static RestTemplate defaultRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return new RestTemplate(factory);
    }

    @Override
    public LocalDateTime confirm(String paymentKey, String orderId, long amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder()
                .encodeToString((keyGuard.secretKey() + ":").getBytes(StandardCharsets.UTF_8)));
        // 주문마다 고정한다. 새로고침으로 같은 주문을 다시 확정하면 토스가 첫 승인 결과를 그대로 돌려준다(최종 검토 I2)
        headers.set("Idempotency-Key", "confirm-" + orderId);
        Map<String, Object> body = Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount);
        try {
            String response = restTemplate.postForObject(CONFIRM_URL, new HttpEntity<>(body, headers), String.class);
            JsonNode approved = objectMapper.readTree(response).path("approvedAt");
            return approved.isTextual()
                    ? OffsetDateTime.parse(approved.asText()).atZoneSameInstant(KST).toLocalDateTime()
                    : LocalDateTime.now(KST);
        } catch (RestClientResponseException e) {
            String code = "UNKNOWN";
            String message = "결제 승인에 실패했습니다.";
            try {
                JsonNode error = objectMapper.readTree(e.getResponseBodyAsString());
                code = error.path("code").asText(code);
                message = error.path("message").asText(message);
            } catch (Exception ignored) {
                // 오류 본문을 못 읽으면 기본 문구로 둔다
            }
            boolean retryable = e.getStatusCode().is5xxServerError();
            log.warn("토스 결제 승인 거절 orderId={} status={} code={}", orderId, e.getStatusCode().value(), code);
            throw new PaymentGatewayException(code, message, retryable);
        } catch (PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.warn("토스 결제 승인 호출 실패 orderId={} error={}", orderId, e.getClass().getSimpleName());
            throw new PaymentGatewayException("NETWORK_ERROR", "결제 승인 중 문제가 생겼습니다. 잠시 뒤 다시 시도해 주세요.", true);
        }
    }
}
