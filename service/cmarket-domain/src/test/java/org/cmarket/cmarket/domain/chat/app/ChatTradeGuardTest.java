package org.cmarket.cmarket.domain.chat.app;

import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomCreateCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListItemDto;
import org.cmarket.cmarket.domain.chat.app.service.ChatReadStatusService;
import org.cmarket.cmarket.domain.chat.app.service.ChatServiceImpl;
import org.cmarket.cmarket.domain.chat.app.service.ChatSessionService;
import org.cmarket.cmarket.domain.chat.app.service.PrivacyFilterService;
import org.cmarket.cmarket.domain.chat.model.ChatRoom;
import org.cmarket.cmarket.domain.chat.model.ChatRoomUser;
import org.cmarket.cmarket.domain.chat.model.MessageType;
import org.cmarket.cmarket.domain.chat.repository.ChatMessageRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomUserRepository;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.payment.model.PaymentStatus;
import org.cmarket.cmarket.domain.payment.repository.PaymentRepository;
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
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 예약된 상품의 채팅을 막는다 (설계 6절). 판매자 1, 예약자 7, 다른 구매자 8.
 * 방 100 = 1·7, 방 200 = 1·8.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatTradeGuardTest {

    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatRoomUserRepository chatRoomUserRepository;
    @Mock ChatMessageRepository chatMessageRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock ChatReadStatusService chatReadStatusService;
    @Mock ChatSessionService chatSessionService;
    @Mock PrivacyFilterService privacyFilterService;
    @Mock UserBlockRepository userBlockRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks ChatServiceImpl chatService;

    Product product;
    final java.util.Map<Long, ChatRoom> rooms = new java.util.HashMap<>();

    @BeforeEach
    void setUp() {
        product = Product.builder().sellerId(1L).productType(ProductType.SELL).title("신발").price(50000L)
                .tradeStatus(TradeStatus.SELLING).build();
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        user("seller@b.com", 1L);
        user("buyer@b.com", 7L);
        user("other@b.com", 8L);
        room(100L, 7L);
        room(200L, 8L);
        when(chatMessageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void user(String email, Long id) {
        User u = mock(User.class);
        when(u.getId()).thenReturn(id);
        when(u.getNickname()).thenReturn("u" + id);
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(u));
        when(userRepository.findById(id)).thenReturn(Optional.of(u));
    }

    private void room(Long roomId, Long buyerId) {
        ChatRoom r = mock(ChatRoom.class);
        when(r.getId()).thenReturn(roomId);
        when(r.getProductId()).thenReturn(10L);
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(r));
        rooms.put(roomId, r);
        ChatRoomUser seller = ChatRoomUser.builder().chatRoomId(roomId).userId(1L).userNickname("판매자").build();
        ChatRoomUser buyer = ChatRoomUser.builder().chatRoomId(roomId).userId(buyerId).userNickname("b" + buyerId).build();
        when(chatRoomUserRepository.findByChatRoomIdAndUserId(roomId, 1L)).thenReturn(Optional.of(seller));
        when(chatRoomUserRepository.findByChatRoomIdAndUserId(roomId, buyerId)).thenReturn(Optional.of(buyer));
        when(chatRoomUserRepository.findOpponentByChatRoomIdAndMyUserId(roomId, 1L)).thenReturn(Optional.of(buyer));
        when(chatRoomUserRepository.findOpponentByChatRoomIdAndMyUserId(roomId, buyerId)).thenReturn(Optional.of(seller));
    }

    private ChatMessageCommand msg(Long roomId) {
        return ChatMessageCommand.builder().chatRoomId(roomId).content("안녕하세요").messageType(MessageType.TEXT).build();
    }

    private void assertCode(Runnable call, ErrorCode code) {
        assertThatThrownBy(call::run).isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode()).isEqualTo(code);
    }

    @Test
    void createChatRoom_reservedForOther_rejected() {
        product.reserveFor(7L);
        assertCode(() -> chatService.createChatRoom("other@b.com", ChatRoomCreateCommand.builder().productId(10L).build()),
                ErrorCode.CHAT_PRODUCT_RESERVED);
    }

    @Test
    void createChatRoom_reservedForOther_existingRoom_returned() {
        // 사용자 결정(2026-11-01): 이미 대화하던 사람은 그 방으로 보낸다. 들어가면 「예약된 상품이에요」가 보이고 보내기는 막힌다
        product.reserveFor(7L);
        when(chatRoomRepository.findByProductIdAndUsers(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(rooms.get(200L)));

        assertThat(chatService.createChatRoom("other@b.com", ChatRoomCreateCommand.builder().productId(10L).build())
                .getChatRoomId()).isEqualTo(200L);
    }

    @Test
    void createChatRoom_reservedBuyer_allowed() {
        product.reserveFor(7L);
        when(chatRoomRepository.findByProductIdAndUsers(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(rooms.get(100L)));
        assertThatCode(() -> chatService.createChatRoom("buyer@b.com", ChatRoomCreateCommand.builder().productId(10L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    void createChatRoom_legacyReserved_allowed() {
        product.updateTradeStatus(TradeStatus.RESERVED);
        when(chatRoomRepository.findByProductIdAndUsers(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.of(rooms.get(200L)));
        assertThatCode(() -> chatService.createChatRoom("other@b.com", ChatRoomCreateCommand.builder().productId(10L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    void sendMessage_buyerToPausedRoom_rejected() {
        product.reserveFor(7L);
        assertCode(() -> chatService.sendMessage("other@b.com", msg(200L)), ErrorCode.CHAT_ROOM_PAUSED);
    }

    @Test
    void sendMessage_sellerToPausedRoom_rejected() {
        product.reserveFor(7L);
        assertCode(() -> chatService.sendMessage("seller@b.com", msg(200L)), ErrorCode.CHAT_ROOM_PAUSED);
    }

    @Test
    void sendMessage_afterPaid_rejected() {
        product.reserveFor(7L);
        product.updateTradeStatus(TradeStatus.COMPLETED);
        when(paymentRepository.existsByProductIdAndStatus(10L, PaymentStatus.DONE)).thenReturn(true);
        assertCode(() -> chatService.sendMessage("buyer@b.com", msg(100L)), ErrorCode.CHAT_ROOM_PAUSED);
    }

    @Test
    void sendMessage_reservedBuyersRoom_allowed() {
        product.reserveFor(7L);
        assertThatCode(() -> chatService.sendMessage("buyer@b.com", msg(100L))).doesNotThrowAnyException();
    }

    @Test
    void roomListItem_hasStateAndCanPay() {
        product.reserveFor(7L);

        ChatRoomListItemDto buyerView = chatService.getChatRoomListItem("buyer@b.com", 100L);
        ChatRoomListItemDto otherView = chatService.getChatRoomListItem("other@b.com", 200L);
        ChatRoomListItemDto sellerView = chatService.getChatRoomListItem("seller@b.com", 100L);

        assertThat(buyerView.getRoomState()).isEqualTo("OPEN");
        assertThat(buyerView.getCanPay()).isTrue();
        assertThat(otherView.getRoomState()).isEqualTo("PAUSED_RESERVED");
        assertThat(otherView.getCanPay()).isFalse();
        assertThat(sellerView.getCanPay()).isFalse();
    }

    @Test
    void roomListItem_hasNoReservedBuyerId() {
        // 예약자가 누구인지는 응답에 싣지 않는다(설계 2절 「실패로 보는 것」)
        assertThat(Arrays.stream(ChatRoomListItemDto.class.getDeclaredFields()).map(f -> f.getName().toLowerCase()))
                .noneMatch(n -> n.contains("reserv") || n.contains("buyerid"));
    }
}
