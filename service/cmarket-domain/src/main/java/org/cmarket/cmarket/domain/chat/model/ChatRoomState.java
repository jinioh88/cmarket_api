package org.cmarket.cmarket.domain.chat.model;

import org.cmarket.cmarket.domain.product.model.TradeStatus;

/**
 * 채팅방이 지금 열려 있는지 (설계 6절)
 *
 * 상품 상태, 예약자, 완료된 결제, 이 방의 구매자로 그때그때 계산한다. 저장하지 않는다.
 * 메시지 보내기·새 채팅 걸기·방 목록 응답이 모두 이 값을 쓴다.
 *
 * 예약자가 비어 있는 RESERVED 는 옛 방식(앱, 이미 예약중이던 상품)이라 지금처럼 막지 않는다.
 */
public enum ChatRoomState {
    OPEN,             // 대화할 수 있다
    PAUSED_RESERVED,  // 다른 사람과 예약되어 잠시 멈췄다
    CLOSED_SOLD,      // 다른 사람이 결제해 판매 완료됐다
    CLOSED_DONE;      // 이 방에서 결제가 끝났다

    public static ChatRoomState of(TradeStatus status, Long reservedBuyerId, boolean paid, Long roomBuyerId) {
        if (status == TradeStatus.RESERVED && reservedBuyerId != null) {
            return reservedBuyerId.equals(roomBuyerId) ? OPEN : PAUSED_RESERVED;
        }
        if (status == TradeStatus.COMPLETED && paid) {
            return reservedBuyerId != null && reservedBuyerId.equals(roomBuyerId) ? CLOSED_DONE : CLOSED_SOLD;
        }
        return OPEN;
    }

    /**
     * 지금 보는 사람이 이 방에서 결제할 수 있는가. 예약자 본인이 자기 방에서만 참이다.
     * 판매자는 늘 거짓이고, 0원은 토스가 받지 않아 거짓이다.
     */
    public static boolean canPay(TradeStatus status, Long reservedBuyerId, boolean paid,
                                 Long price, Long viewerId, Long roomBuyerId) {
        return status == TradeStatus.RESERVED
                && reservedBuyerId != null
                && !paid
                && price != null && price > 0
                && reservedBuyerId.equals(viewerId)
                && reservedBuyerId.equals(roomBuyerId);
    }
}
