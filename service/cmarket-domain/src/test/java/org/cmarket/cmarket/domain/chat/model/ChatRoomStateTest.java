package org.cmarket.cmarket.domain.chat.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cmarket.cmarket.domain.chat.model.ChatRoomState.CLOSED_DONE;
import static org.cmarket.cmarket.domain.chat.model.ChatRoomState.CLOSED_SOLD;
import static org.cmarket.cmarket.domain.chat.model.ChatRoomState.OPEN;
import static org.cmarket.cmarket.domain.chat.model.ChatRoomState.PAUSED_RESERVED;
import static org.cmarket.cmarket.domain.product.model.TradeStatus.COMPLETED;
import static org.cmarket.cmarket.domain.product.model.TradeStatus.RESERVED;
import static org.cmarket.cmarket.domain.product.model.TradeStatus.SELLING;

/**
 * 설계 6절 「채팅방이 열리는지」 표를 그대로 옮겼다.
 * 예약자 7, 다른 구매자 8, 판매자 1.
 */
class ChatRoomStateTest {

    @Test void of_selling_open()                  { assertThat(ChatRoomState.of(SELLING, null, false, 7L)).isEqualTo(OPEN); }
    @Test void of_reservedWithoutBuyer_open()     { assertThat(ChatRoomState.of(RESERVED, null, false, 7L)).isEqualTo(OPEN); }
    @Test void of_reservedBuyersRoom_open()       { assertThat(ChatRoomState.of(RESERVED, 7L, false, 7L)).isEqualTo(OPEN); }
    @Test void of_reservedOthersRoom_paused()     { assertThat(ChatRoomState.of(RESERVED, 7L, false, 8L)).isEqualTo(PAUSED_RESERVED); }
    @Test void of_completedPaid_buyersRoom_done() { assertThat(ChatRoomState.of(COMPLETED, 7L, true, 7L)).isEqualTo(CLOSED_DONE); }
    @Test void of_completedPaid_othersRoom_sold() { assertThat(ChatRoomState.of(COMPLETED, 7L, true, 8L)).isEqualTo(CLOSED_SOLD); }
    @Test void of_completedUnpaid_open()          { assertThat(ChatRoomState.of(COMPLETED, 7L, false, 8L)).isEqualTo(OPEN); }
    @Test void of_requestProductWithoutStatus_open() { assertThat(ChatRoomState.of(null, null, false, 7L)).isEqualTo(OPEN); }

    @Test void canPay_reservedBuyerInOwnRoom_true() { assertThat(ChatRoomState.canPay(RESERVED, 7L, false, 50000L, 7L, 7L)).isTrue(); }
    @Test void canPay_sellerNeverTrue()             { assertThat(ChatRoomState.canPay(RESERVED, 7L, false, 50000L, 1L, 7L)).isFalse(); }
    @Test void canPay_zeroPrice_false()             { assertThat(ChatRoomState.canPay(RESERVED, 7L, false, 0L, 7L, 7L)).isFalse(); }
    @Test void canPay_alreadyPaid_false()           { assertThat(ChatRoomState.canPay(COMPLETED, 7L, true, 50000L, 7L, 7L)).isFalse(); }
    @Test void canPay_legacyReserved_false()        { assertThat(ChatRoomState.canPay(RESERVED, null, false, 50000L, 7L, 7L)).isFalse(); }
}
