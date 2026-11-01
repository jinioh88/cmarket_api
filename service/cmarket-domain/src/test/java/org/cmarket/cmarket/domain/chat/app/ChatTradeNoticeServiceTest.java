package org.cmarket.cmarket.domain.chat.app;

import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;
import org.cmarket.cmarket.domain.chat.app.service.ChatReadStatusService;
import org.cmarket.cmarket.domain.chat.app.service.ChatTradeNoticeService;
import org.cmarket.cmarket.domain.chat.model.ChatMessage;
import org.cmarket.cmarket.domain.chat.model.ChatRoom;
import org.cmarket.cmarket.domain.chat.model.ChatRoomUser;
import org.cmarket.cmarket.domain.chat.model.MessageType;
import org.cmarket.cmarket.domain.chat.repository.ChatMessageRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 예약이 걸리거나 풀리거나 결제가 끝날 때, 그 상품의 방마다 남기는 한 줄 (설계 6절).
 * 판매자 1, 예약자 7, 다른 구매자 8. 방 100 = 예약자 방, 방 200 = 다른 사람 방.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatTradeNoticeServiceTest {

    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatRoomUserRepository chatRoomUserRepository;
    @Mock ChatMessageRepository chatMessageRepository;
    @Mock ChatReadStatusService chatReadStatusService;

    @InjectMocks ChatTradeNoticeService noticeService;

    ChatRoomUser sellerIn100, buyerIn100, sellerIn200, otherIn200;

    @BeforeEach
    void setUp() {
        ChatRoom room100 = mock(ChatRoom.class);
        when(room100.getId()).thenReturn(100L);
        ChatRoom room200 = mock(ChatRoom.class);
        when(room200.getId()).thenReturn(200L);
        when(chatRoomRepository.findAllByProductId(10L)).thenReturn(List.of(room100, room200));

        sellerIn100 = user(100L, 1L, "판매자");
        buyerIn100 = user(100L, 7L, "예약자");
        sellerIn200 = user(200L, 1L, "판매자");
        otherIn200 = user(200L, 8L, "다른분");
        when(chatRoomUserRepository.findByChatRoomId(100L)).thenReturn(List.of(sellerIn100, buyerIn100));
        when(chatRoomUserRepository.findByChatRoomId(200L)).thenReturn(List.of(sellerIn200, otherIn200));
        when(chatMessageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static ChatRoomUser user(Long roomId, Long userId, String nickname) {
        return ChatRoomUser.builder().chatRoomId(roomId).userId(userId).userNickname(nickname).build();
    }

    @Test
    void post_buyerAndOthersGetTheirOwnLine() {
        List<ChatSystemNotice> notices = noticeService.post(10L, 1L, 7L, "예약됐어요. 여기서 결제할 수 있어요",
                "판매자가 다른 분과 예약해서 대화가 잠시 멈췄어요");

        assertThat(notices).containsExactlyInAnyOrder(
                new ChatSystemNotice(100L, "예약됐어요. 여기서 결제할 수 있어요"),
                new ChatSystemNotice(200L, "판매자가 다른 분과 예약해서 대화가 잠시 멈췄어요"));
        ArgumentCaptor<ChatMessage> saved = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).allMatch(m -> m.getMessageType() == MessageType.SYSTEM);
        assertThat(otherIn200.getLastMessageContent()).isEqualTo("판매자가 다른 분과 예약해서 대화가 잠시 멈췄어요");
        verify(chatReadStatusService).incrementUnreadCount(200L, 8L);
    }

    @Test
    void post_nullOthersText_onlyBuyersRoom() {
        // 결제 완료는 예약자 방에만 남긴다(설계 6절)
        List<ChatSystemNotice> notices = noticeService.post(10L, 1L, 7L, "결제가 완료됐어요", null);

        assertThat(notices).containsExactly(new ChatSystemNotice(100L, "결제가 완료됐어요"));
        verify(chatReadStatusService, never()).incrementUnreadCount(200L, 8L);
    }

    @Test
    void post_skipsRoomWhereBuyerLeft() {
        otherIn200.leave();

        List<ChatSystemNotice> notices = noticeService.post(10L, 1L, 7L, "예약이 풀렸어요", "예약이 풀려서 다시 대화할 수 있어요");

        assertThat(notices).containsExactly(new ChatSystemNotice(100L, "예약이 풀렸어요"));
    }
}
