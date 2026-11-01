package org.cmarket.cmarket.domain.product.app;

import org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;
import org.cmarket.cmarket.domain.chat.app.service.ChatTradeNoticeService;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.payment.model.PaymentStatus;
import org.cmarket.cmarket.domain.payment.repository.PaymentRepository;
import org.cmarket.cmarket.domain.product.app.dto.TradeStatusChangeResult;
import org.cmarket.cmarket.domain.product.app.dto.TradeStatusUpdateCommand;
import org.cmarket.cmarket.domain.product.app.service.ProductServiceImpl;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;
import org.cmarket.cmarket.domain.product.repository.FavoriteRepository;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.cmarket.cmarket.domain.report.repository.UserBlockRepository;
import org.cmarket.cmarket.domain.view.app.service.ViewLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 설계 6절 「상태 바꾸기 규칙」. 판매자 1, 예약자 7.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductTradeStatusRuleTest {

    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock FavoriteRepository favoriteRepository;
    @Mock UserBlockRepository userBlockRepository;
    @Mock ViewLogService viewLogService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock PaymentRepository paymentRepository;
    @Mock ChatTradeNoticeService chatTradeNoticeService;

    @InjectMocks ProductServiceImpl productService;

    Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder().sellerId(1L).productType(ProductType.SELL).title("신발").price(50000L)
                .tradeStatus(TradeStatus.SELLING).build();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        User seller = mock(User.class);
        when(seller.getId()).thenReturn(1L);
        when(userRepository.findByEmailAndDeletedAtIsNull("seller@b.com")).thenReturn(Optional.of(seller));
        when(favoriteRepository.findUserIdsByProductId(anyLong())).thenReturn(List.of());
    }

    private TradeStatusChangeResult patch(TradeStatus to) {
        return productService.updateTradeStatus(10L, new TradeStatusUpdateCommand(to), "seller@b.com");
    }

    @Test
    void patch_sellingToReserved_legacyAllowed_noBuyer() {
        TradeStatusChangeResult result = patch(TradeStatus.RESERVED);

        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.RESERVED);
        assertThat(product.getReservedBuyerId()).isNull();
        assertThat(result.notices()).isEmpty();
    }

    @Test
    void patch_reservedWithBuyerToReserved_rejected() {
        product.reserveFor(7L);

        assertThatThrownBy(() -> patch(TradeStatus.RESERVED))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRADE_STATUS_CHANGE_NOT_ALLOWED);
    }

    @Test
    void patch_reservedWithBuyerToSelling_clearsAndNotices() {
        product.reserveFor(7L);
        List<ChatSystemNotice> notices = List.of(new ChatSystemNotice(100L, "예약이 풀렸어요"));
        when(chatTradeNoticeService.post(10L, 1L, 7L, "예약이 풀렸어요", "예약이 풀려서 다시 대화할 수 있어요"))
                .thenReturn(notices);

        TradeStatusChangeResult result = patch(TradeStatus.SELLING);

        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.SELLING);
        assertThat(product.getReservedBuyerId()).isNull();
        assertThat(result.notices()).isEqualTo(notices);
    }

    @Test
    void patch_reservedWithoutBuyerToSelling_noNotices() {
        product.updateTradeStatus(TradeStatus.RESERVED);

        TradeStatusChangeResult result = patch(TradeStatus.SELLING);

        assertThat(result.notices()).isEmpty();
        verify(chatTradeNoticeService, never()).post(any(), any(), any(), any(), any());
    }

    @Test
    void patch_reservedToCompleted_manualAllowed() {
        product.reserveFor(7L);

        patch(TradeStatus.COMPLETED);

        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(product.getReservedBuyerId()).isEqualTo(7L);
    }

    @Test
    void patch_completedWithDonePaymentToSelling_rejected() {
        product.updateTradeStatus(TradeStatus.COMPLETED);
        when(paymentRepository.existsByProductIdAndStatus(10L, PaymentStatus.DONE)).thenReturn(true);

        assertThatThrownBy(() -> patch(TradeStatus.SELLING))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PAID_PRODUCT_STATUS_LOCKED);
    }

    @Test
    void patch_completedWithoutPaymentToSelling_allowed() {
        product.updateTradeStatus(TradeStatus.COMPLETED);

        patch(TradeStatus.SELLING);

        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.SELLING);
    }

    @Test
    void patch_byNonSeller_rejected() {
        User other = mock(User.class);
        when(other.getId()).thenReturn(8L);
        when(userRepository.findByEmailAndDeletedAtIsNull("other@b.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> productService.updateTradeStatus(10L, new TradeStatusUpdateCommand(TradeStatus.SELLING), "other@b.com"))
                .isInstanceOf(AuthenticationFailedException.class);
    }
}
