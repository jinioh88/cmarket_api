package org.cmarket.cmarket.domain.payment.app;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;
import org.cmarket.cmarket.domain.chat.app.service.ChatTradeNoticeService;
import org.cmarket.cmarket.domain.chat.model.ChatRoomState;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentConfirmCommand;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentOrderDto;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentResultDto;
import org.cmarket.cmarket.domain.payment.model.Payment;
import org.cmarket.cmarket.domain.payment.model.PaymentStatus;
import org.cmarket.cmarket.domain.payment.repository.PaymentRepository;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.TradeStatus;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 토스페이먼츠 테스트 결제 (설계 8절, #1126)
 *
 * 1) prepareOrder: 예약자만, 금액 = 지금 상품 가격으로 주문을 저장한다
 * 2) (웹이 토스 결제창을 연다)
 * 3) confirm: 받은 금액 = 저장한 금액 = 지금 상품 가격이고 아직 이 사람으로 예약중일 때만 토스 승인을 부른다
 *
 * 토스 공식 문서의 필수 검증: 웹이 보낸 금액은 바꿔치기될 수 있어 저장한 값과 대조한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAID_NOTICE = "결제가 완료됐어요";
    // 다른 사람의 방에도 남겨 화면이 바로 「판매 완료」로 바뀌게 한다(2026-11-01 사용자 결정). 누가 샀는지는 넣지 않는다
    private static final String SOLD_NOTICE = "판매가 완료됐어요";

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;
    private final ChatTradeNoticeService chatTradeNoticeService;
    private final org.cmarket.cmarket.domain.chat.repository.ChatRoomRepository chatRoomRepository;

    @Transactional
    public PaymentOrderDto prepareOrder(String buyerEmail, Long productId) {
        User buyer = findUser(buyerEmail);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED));
        boolean paid = paymentRepository.existsByProductIdAndStatus(productId, PaymentStatus.DONE);
        // 채팅방의 결제하기 단추와 같은 조건 (예약자 본인, 0원 아님, 결제 전)
        if (!ChatRoomState.canPay(product.getTradeStatus(), product.getReservedBuyerId(), paid,
                product.getPrice(), buyer.getId(), buyer.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED);
        }
        String orderId = "CM" + UUID.randomUUID().toString().replace("-", "");
        paymentRepository.save(Payment.builder()
                .orderId(orderId)
                .productId(productId)
                .buyerId(buyer.getId())
                .sellerId(product.getSellerId())
                .amount(product.getPrice())
                .build());
        String orderName = product.getTitle().length() > 100 ? product.getTitle().substring(0, 100) : product.getTitle();
        return new PaymentOrderDto(orderId, product.getPrice(), orderName);
    }

    /**
     * 실패(토스 거절)는 기록으로 남겨야 하므로 BusinessException 이 나도 되돌리지 않는다.
     * 거절 전의 검사들은 아무것도 쓰지 않은 채 던진다.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public PaymentResultDto confirm(String buyerEmail, PaymentConfirmCommand cmd) {
        User buyer = findUser(buyerEmail);
        Payment payment = paymentRepository.findByOrderIdForUpdate(cmd.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED));
        if (!payment.getBuyerId().equals(buyer.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED);
        }
        Product product = productRepository.findByIdAndDeletedAtIsNull(payment.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED));

        // 성공 페이지를 새로고침하면 같은 주문이 다시 온다. 토스를 다시 부르지 않고 끝난 결과를 돌려준다
        if (payment.getStatus() == PaymentStatus.DONE) {
            return result(payment, product, List.of());
        }
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED);
        }
        if (cmd.amount() != payment.getAmount()) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (paymentRepository.existsByProductIdAndStatus(payment.getProductId(), PaymentStatus.DONE)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED);
        }
        if (product.getTradeStatus() != TradeStatus.RESERVED || !buyer.getId().equals(product.getReservedBuyerId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_ALLOWED);
        }
        if (!payment.getAmount().equals(product.getPrice())) {
            // 주문한 뒤에 판매자가 가격을 바꿨다. 다시 주문하면 바뀐 가격으로 결제할 수 있다
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        LocalDateTime approvedAt;
        try {
            approvedAt = paymentGateway.confirm(cmd.paymentKey(), cmd.orderId(), payment.getAmount());
        } catch (PaymentGatewayException e) {
            if (e.isRetryable()) {
                // 답을 못 받았을 뿐 승인됐을 수 있다. READY 로 두면 새로고침이 같은 Idempotency-Key 로 다시 확정한다
                throw new BusinessException(ErrorCode.PAYMENT_RETRY_LATER);
            }
            payment.markFailed(e.getMessage());
            paymentRepository.save(payment);
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        payment.markDone(cmd.paymentKey(), approvedAt);
        paymentRepository.save(payment);
        product.updateTradeStatus(TradeStatus.COMPLETED);
        productRepository.save(product);
        List<ChatSystemNotice> notices = chatTradeNoticeService.post(
                payment.getProductId(), product.getSellerId(), buyer.getId(), PAID_NOTICE, SOLD_NOTICE);
        return result(payment, product, notices);
    }

    private PaymentResultDto result(Payment payment, Product product, List<ChatSystemNotice> notices) {
        Long chatRoomId = chatRoomRepository
                .findByProductIdAndUsers(payment.getProductId(), payment.getBuyerId(), payment.getSellerId())
                .map(org.cmarket.cmarket.domain.chat.model.ChatRoom::getId)
                .orElse(null);
        return new PaymentResultDto(payment.getOrderId(), payment.getAmount(), payment.getProductId(),
                product.getTitle(), product.getMainImageUrl(), payment.getApprovedAt(), chatRoomId, notices);
    }

    private User findUser(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
