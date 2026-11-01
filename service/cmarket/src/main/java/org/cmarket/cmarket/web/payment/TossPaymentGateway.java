package org.cmarket.cmarket.web.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.payment.app.PaymentGateway;
import org.cmarket.cmarket.domain.payment.app.PaymentGatewayException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

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

    private final TossKeyGuard keyGuard;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TossPaymentGateway(TossKeyGuard keyGuard) {
        this.keyGuard = keyGuard;
    }

    @Override
    public LocalDateTime confirm(String paymentKey, String orderId, long amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder()
                .encodeToString((keyGuard.secretKey() + ":").getBytes(StandardCharsets.UTF_8)));
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
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
            log.warn("토스 결제 승인 거절 orderId={} status={} code={}", orderId, e.getStatusCode().value(), code);
            throw new PaymentGatewayException(code, message);
        } catch (PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.warn("토스 결제 승인 호출 실패 orderId={} error={}", orderId, e.getClass().getSimpleName());
            throw new PaymentGatewayException("NETWORK_ERROR", "결제 승인 중 문제가 생겼습니다. 잠시 뒤 다시 시도해 주세요.");
        }
    }
}
