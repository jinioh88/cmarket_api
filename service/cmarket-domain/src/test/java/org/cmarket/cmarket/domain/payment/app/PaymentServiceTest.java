package org.cmarket.cmarket.domain.payment.app;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;
import org.cmarket.cmarket.domain.chat.app.service.ChatTradeNoticeService;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentConfirmCommand;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentOrderDto;
import org.cmarket.cmarket.domain.payment.app.dto.PaymentResultDto;
import org.cmarket.cmarket.domain.payment.model.Payment;
import org.cmarket.cmarket.domain.payment.model.PaymentStatus;
import org.cmarket.cmarket.domain.payment.repository.PaymentRepository;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 설계 8절. 판매자 1, 예약자 7, 다른 사람 8. 상품 10 (50,000원), 주문 "CM-ORDER-1".
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentGateway paymentGateway;
    @Mock ChatTradeNoticeService chatTradeNoticeService;

    @InjectMocks PaymentService paymentService;

    Product product;
    Payment order;
    final LocalDateTime approvedAt = LocalDateTime.of(2026, 11, 1, 15, 0);

    @BeforeEach
    void setUp() {
        product = Product.builder().sellerId(1L).productType(ProductType.SELL).title("신발").price(50000L)
                .tradeStatus(TradeStatus.SELLING).build();
        product.reserveFor(7L);
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        user("buyer@b.com", 7L);
        user("other@b.com", 8L);
        order = Payment.builder().orderId("CM-ORDER-1").productId(10L).buyerId(7L).sellerId(1L).amount(50000L).build();
        when(paymentRepository.findByOrderIdForUpdate("CM-ORDER-1")).thenReturn(Optional.of(order));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.confirm(anyString(), anyString(), anyLong())).thenReturn(approvedAt);
    }

    private void user(String email, Long id) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(u));
    }

    private PaymentConfirmCommand cmd(long amount) {
        return new PaymentConfirmCommand("pk_1", "CM-ORDER-1", amount);
    }

    private void assertCode(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode()).isEqualTo(code);
    }

    @Test
    void prepareOrder_reservedBuyer_savesReadyWithCurrentPrice() {
        PaymentOrderDto dto = paymentService.prepareOrder("buyer@b.com", 10L);

        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(saved.getValue().getAmount()).isEqualTo(50000L);
        assertThat(saved.getValue().getBuyerId()).isEqualTo(7L);
        assertThat(dto.amount()).isEqualTo(50000L);
        assertThat(dto.orderName()).isEqualTo("신발");
        assertThat(dto.orderId()).matches("[A-Za-z0-9_-]{6,64}");
    }

    @Test
    void prepareOrder_notReservedBuyer_rejected() {
        assertCode(() -> paymentService.prepareOrder("other@b.com", 10L), ErrorCode.PAYMENT_NOT_ALLOWED);
    }

    @Test
    void prepareOrder_zeroPrice_rejected() {
        Product free = Product.builder().sellerId(1L).productType(ProductType.SELL).title("나눔").price(0L).build();
        free.reserveFor(7L);
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(free));
        assertCode(() -> paymentService.prepareOrder("buyer@b.com", 10L), ErrorCode.PAYMENT_NOT_ALLOWED);
    }

    @Test
    void confirm_ok_marksDone_completesProduct_postsNotice() {
        List<ChatSystemNotice> notices = List.of(new ChatSystemNotice(100L, "결제가 완료됐어요"));
        when(chatTradeNoticeService.post(10L, 1L, 7L, "결제가 완료됐어요", null)).thenReturn(notices);

        PaymentResultDto result = paymentService.confirm("buyer@b.com", cmd(50000L));

        assertThat(order.getStatus()).isEqualTo(PaymentStatus.DONE);
        assertThat(order.getPaymentKey()).isEqualTo("pk_1");
        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(result.notices()).isEqualTo(notices);
        assertThat(result.approvedAt()).isEqualTo(approvedAt);
        verify(paymentGateway).confirm("pk_1", "CM-ORDER-1", 50000L);
    }

    @Test
    void confirm_amountDiffersFromOrder_rejected_noGateway() {
        assertCode(() -> paymentService.confirm("buyer@b.com", cmd(100L)), ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        verify(paymentGateway, never()).confirm(anyString(), anyString(), anyLong());
    }

    @Test
    void confirm_priceChanged_rejected() {
        // 판매자가 주문 뒤에 가격을 깎았다
        product.update(product.getPetType(), product.getPetDetailType(), product.getCategory(), product.getTitle(),
                product.getDescription(), 45000L, product.getProductStatus(), product.getMainImageUrl(),
                product.getSubImageUrls(), product.getAddressSido(), product.getAddressGugun());
        assertCode(() -> paymentService.confirm("buyer@b.com", cmd(50000L)), ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        verify(paymentGateway, never()).confirm(anyString(), anyString(), anyLong());
    }

    @Test
    void confirm_reservationCleared_rejected() {
        product.clearReservation();
        product.updateTradeStatus(TradeStatus.SELLING);
        assertCode(() -> paymentService.confirm("buyer@b.com", cmd(50000L)), ErrorCode.PAYMENT_NOT_ALLOWED);
        verify(paymentGateway, never()).confirm(anyString(), anyString(), anyLong());
    }

    @Test
    void confirm_othersOrder_rejected() {
        assertCode(() -> paymentService.confirm("other@b.com", cmd(50000L)), ErrorCode.PAYMENT_NOT_ALLOWED);
    }

    @Test
    void confirm_alreadyDone_returnsSameResultWithoutCallingGateway() {
        order.markDone("pk_1", approvedAt);

        PaymentResultDto result = paymentService.confirm("buyer@b.com", cmd(50000L));

        assertThat(result.orderId()).isEqualTo("CM-ORDER-1");
        assertThat(result.approvedAt()).isEqualTo(approvedAt);
        verify(paymentGateway, never()).confirm(anyString(), anyString(), anyLong());
    }

    @Test
    void confirm_secondOrderAfterDone_rejected() {
        when(paymentRepository.existsByProductIdAndStatus(10L, PaymentStatus.DONE)).thenReturn(true);
        assertCode(() -> paymentService.confirm("buyer@b.com", cmd(50000L)), ErrorCode.PAYMENT_NOT_ALLOWED);
        verify(paymentGateway, never()).confirm(anyString(), anyString(), anyLong());
    }

    @Test
    void confirm_gatewayNetworkError_keepsReady() {
        // 최종 검토 I2: 연결이 끊겼을 뿐 토스는 승인했을 수 있다. 실패로 닫지 않고 새로고침으로 다시 확정하게 둔다
        when(paymentGateway.confirm(anyString(), anyString(), anyLong()))
                .thenThrow(new PaymentGatewayException("NETWORK_ERROR", "결제 승인 중 문제가 생겼습니다.", true));

        assertCode(() -> paymentService.confirm("buyer@b.com", cmd(50000L)), ErrorCode.PAYMENT_RETRY_LATER);
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.RESERVED);
    }

    @Test
    void confirm_gatewayFails_marksFailed() {
        when(paymentGateway.confirm(anyString(), anyString(), anyLong()))
                .thenThrow(new PaymentGatewayException("REJECT_CARD_COMPANY", "카드사에서 거절했습니다."));

        assertCode(() -> paymentService.confirm("buyer@b.com", cmd(50000L)), ErrorCode.PAYMENT_FAILED);
        assertThat(order.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getFailReason()).isEqualTo("카드사에서 거절했습니다.");
        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.RESERVED);
    }
}
