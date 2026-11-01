package org.cmarket.cmarket.domain.chat.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.chat.app.dto.ChatSystemNotice;
import org.cmarket.cmarket.domain.chat.model.ChatMessage;
import org.cmarket.cmarket.domain.chat.model.ChatRoom;
import org.cmarket.cmarket.domain.chat.model.ChatRoomUser;
import org.cmarket.cmarket.domain.chat.model.MessageType;
import org.cmarket.cmarket.domain.chat.repository.ChatMessageRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 예약이 걸리거나 풀리거나 결제가 끝날 때 그 상품의 방마다 한 줄을 남긴다 (설계 6절, #1126)
 *
 * 문구에는 예약자가 누구인지 넣지 않는다. 구매자가 나간 방에는 남기지 않는다.
 * 저장만 하고, 웹소켓으로 보내는 일은 돌려준 목록을 받은 웹 계층이 한다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ChatTradeNoticeService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReadStatusService chatReadStatusService;

    /**
     * @param buyerText  예약자 방에 남길 말 (null 이면 남기지 않는다)
     * @param othersText 나머지 방에 남길 말 (null 이면 남기지 않는다)
     */
    public List<ChatSystemNotice> post(Long productId, Long sellerId, Long reservedBuyerId,
                                       String buyerText, String othersText) {
        List<ChatSystemNotice> notices = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (ChatRoom room : chatRoomRepository.findAllByProductId(productId)) {
            List<ChatRoomUser> users = chatRoomUserRepository.findByChatRoomId(room.getId());
            Optional<ChatRoomUser> buyer = users.stream().filter(u -> !u.getUserId().equals(sellerId)).findFirst();
            if (buyer.isEmpty() || !Boolean.TRUE.equals(buyer.get().getIsActive())) {
                continue;
            }
            boolean isReservedBuyersRoom = buyer.get().getUserId().equals(reservedBuyerId);
            String content = isReservedBuyersRoom ? buyerText : othersText;
            if (content == null) {
                continue;
            }
            String sellerNickname = users.stream().filter(u -> u.getUserId().equals(sellerId))
                    .map(ChatRoomUser::getUserNickname).findFirst().orElse(null);
            chatMessageRepository.save(ChatMessage.builder()
                    .chatRoomId(room.getId())
                    .senderId(sellerId)
                    .senderNickname(sellerNickname)
                    .messageType(MessageType.SYSTEM)
                    .content(content)
                    .build());
            users.forEach(u -> u.updateLastMessage(content, now, false));
            chatReadStatusService.incrementUnreadCount(room.getId(), buyer.get().getUserId());
            notices.add(new ChatSystemNotice(room.getId(), content));
        }
        return notices;
    }
}
