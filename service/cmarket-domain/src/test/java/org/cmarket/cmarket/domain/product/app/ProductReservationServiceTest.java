package org.cmarket.cmarket.domain.product.app;

import org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;
import org.cmarket.cmarket.domain.chat.app.service.ChatTradeNoticeService;
import org.cmarket.cmarket.domain.chat.model.ChatRoom;
import org.cmarket.cmarket.domain.chat.model.ChatRoomUser;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomUserRepository;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.product.app.dto.ChatPartnerDto;
import org.cmarket.cmarket.domain.product.app.service.ProductReservationService;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.cmarket.cmarket.domain.report.repository.UserBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 설계 7절. 판매자 1. 방 100 = 7(오래전 대화), 방 200 = 8(최근 대화), 방 300 = 9(나감), 방 400 = 5(차단), 방 500 = 6(탈퇴)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductReservationServiceTest {

    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatRoomUserRepository chatRoomUserRepository;
    @Mock UserBlockRepository userBlockRepository;
    @Mock ChatTradeNoticeService chatTradeNoticeService;

    @InjectMocks ProductReservationService reservationService;

    Product product;
    final LocalDateTime base = LocalDateTime.of(2026, 11, 1, 12, 0);

    @BeforeEach
    void setUp() {
        product = Product.builder().sellerId(1L).productType(ProductType.SELL).title("신발").price(50000L)
                .tradeStatus(TradeStatus.SELLING).build();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        mockUser("seller@b.com", 1L);
        mockUser("other@b.com", 8L);

        room(100L, 7L, "신발사요", "a.png", base.minusDays(2), true);
        room(200L, 8L, "구경만", "b.png", base, true);
        room(300L, 9L, "나간분", null, base, false);
        room(400L, 5L, "차단된분", null, base, true);
        room(500L, 6L, "탈퇴한분", null, base, true);
        List<ChatRoom> rooms = List.of(100L, 200L, 300L, 400L, 500L).stream().map(id -> {
            ChatRoom r = mock(ChatRoom.class);
            when(r.getId()).thenReturn(id);
            return r;
        }).toList();
        when(chatRoomRepository.findAllByProductId(10L)).thenReturn(rooms);
        when(userBlockRepository.existsByBlockerIdAndBlockedUserId(1L, 5L)).thenReturn(true);
        when(userRepository.findByIdAndDeletedAtIsNull(anyLong())).thenAnswer(inv ->
                inv.getArgument(0).equals(6L) ? Optional.empty() : Optional.of(mock(User.class)));
    }

    private void mockUser(String email, Long id) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(u));
    }

    private void room(Long roomId, Long buyerId, String nickname, String photo, LocalDateTime lastAt, boolean active) {
        ChatRoomUser seller = ChatRoomUser.builder().chatRoomId(roomId).userId(1L).userNickname("판매자").build();
        ChatRoomUser buyer = ChatRoomUser.builder().chatRoomId(roomId).userId(buyerId).userNickname(nickname)
                .userProfileImageUrl(photo).build();
        buyer.updateLastMessage("안녕하세요", lastAt, false);
        if (!active) buyer.leave();
        when(chatRoomUserRepository.findByChatRoomId(roomId)).thenReturn(List.of(seller, buyer));
    }

    @Test
    void listChatPartners_returnsSnapshotNicknameAndPhoto() {
        List<ChatPartnerDto> partners = reservationService.listChatPartners(10L, "seller@b.com");

        assertThat(partners).extracting(ChatPartnerDto::userId).containsExactly(8L, 7L);
        assertThat(partners.get(1)).isEqualTo(new ChatPartnerDto(7L, "신발사요", "a.png", base.minusDays(2)));
    }

    @Test
    void listChatPartners_excludesLeft_blocked_withdrawn() {
        assertThat(reservationService.listChatPartners(10L, "seller@b.com"))
                .extracting(ChatPartnerDto::userId).doesNotContain(9L, 5L, 6L);
    }

    @Test
    void listChatPartners_byNonSeller_rejected() {
        assertThatThrownBy(() -> reservationService.listChatPartners(10L, "other@b.com"))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void reserve_partner_setsBuyerAndPostsNotices() {
        List<ChatSystemNotice> notices = List.of(new ChatSystemNotice(100L, "예약됐어요. 여기서 결제할 수 있어요"));
        when(chatTradeNoticeService.post(10L, 1L, 7L, "예약됐어요. 여기서 결제할 수 있어요",
                "판매자가 다른 분과 예약해서 대화가 잠시 멈췄어요")).thenReturn(notices);

        List<ChatSystemNotice> result = reservationService.reserve(10L, 7L, "seller@b.com");

        assertThat(product.getTradeStatus()).isEqualTo(TradeStatus.RESERVED);
        assertThat(product.getReservedBuyerId()).isEqualTo(7L);
        assertThat(result).isEqualTo(notices);
    }

    @Test
    void reserve_notPartner_rejected() {
        assertRejected(() -> reservationService.reserve(10L, 9L, "seller@b.com"), ErrorCode.RESERVATION_BUYER_NOT_PARTNER);
        assertThat(product.getReservedBuyerId()).isNull();
    }

    @Test
    void reserve_alreadyReserved_rejected() {
        product.reserveFor(8L);
        assertRejected(() -> reservationService.reserve(10L, 7L, "seller@b.com"), ErrorCode.RESERVATION_NOT_ALLOWED);
    }

    @Test
    void reserve_requestProduct_rejected() {
        Product request = Product.builder().sellerId(1L).productType(ProductType.REQUEST).title("구해요").price(1000L).build();
        when(productRepository.findByIdAndDeletedAtIsNull(11L)).thenReturn(Optional.of(request));
        assertRejected(() -> reservationService.reserve(11L, 7L, "seller@b.com"), ErrorCode.RESERVATION_NOT_ALLOWED);
    }

    @Test
    void reserve_zeroPrice_allowed() {
        Product free = Product.builder().sellerId(1L).productType(ProductType.SELL).title("나눔").price(0L)
                .tradeStatus(TradeStatus.SELLING).build();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(free));

        reservationService.reserve(10L, 7L, "seller@b.com");

        assertThat(free.getReservedBuyerId()).isEqualTo(7L);
    }

    @Test
    void reserve_byNonSeller_rejected() {
        assertThatThrownBy(() -> reservationService.reserve(10L, 7L, "other@b.com"))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    private void assertRejected(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode()).isEqualTo(code);
    }
}
