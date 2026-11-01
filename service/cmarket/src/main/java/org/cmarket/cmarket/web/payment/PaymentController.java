package org.cmarket.cmarket.web.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.payment.app.PaymentService;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentOrderDto;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentResultDto;
import org.cmarket.cmarket.web.chat.controller.ChatWebSocketController;
import org.cmarket.cmarket.web.common.response.ErrorResponse;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.common.security.SecurityUtils;
import org.cmarket.cmarket.web.payment.dto.PaymentConfirmRequest;
import org.cmarket.cmarket.web.payment.dto.PaymentOrderRequest;
import org.cmarket.cmarket.web.payment.dto.PaymentResultResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 토스페이먼츠 테스트 결제 (#1126, 설계 8절)
 *
 * - POST /api/payments/orders   예약자가 결제하기를 누르면 주문을 만든다 (금액은 서버가 정한다)
 * - POST /api/payments/confirm  결제 성공 주소로 돌아온 값으로 승인을 확정한다
 *
 * 서버의 비밀 키가 테스트 키가 아니면 둘 다 503 이다.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final TossKeyGuard keyGuard;
    private final ChatWebSocketController chatWebSocketController;

    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrder(@Valid @RequestBody PaymentOrderRequest request) {
        if (!keyGuard.isUsable()) {
            return unavailable();
        }
        PaymentOrderDto order = paymentService.prepareOrder(SecurityUtils.getCurrentUserEmail(), request.productId());
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, order));
    }

    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> confirm(@Valid @RequestBody PaymentConfirmRequest request) {
        if (!keyGuard.isUsable()) {
            return unavailable();
        }
        PaymentResultDto result = paymentService.confirm(SecurityUtils.getCurrentUserEmail(), request.toCommand());
        result.notices().forEach(n -> chatWebSocketController.sendSystemMessage(n.chatRoomId(), n.content()));
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, PaymentResultResponse.fromDto(result)));
    }

    private ResponseEntity<ErrorResponse> unavailable() {
        String traceId = MDC.get("traceId");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(ResponseCode.SERVICE_UNAVAILABLE, "결제 준비 중입니다.",
                        traceId != null ? traceId : "unknown"));
    }
}
