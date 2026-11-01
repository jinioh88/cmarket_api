package org.cmarket.cmarket.domain.product.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.app.exception.AuthenticationFailedException;
import org.cmarket.cmarket.domain.auth.app.exception.UserNotFoundException;
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
import org.cmarket.cmarket.domain.product.app.exception.ProductNotFoundException;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.model.ProductType;
import org.cmarket.cmarket.domain.product.model.TradeStatus;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.cmarket.cmarket.domain.report.repository.UserBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 판매자가 채팅한 상대를 골라 예약한다 (설계 7절, #1126)
 *
 * 상대 목록은 판매자 본인만 볼 수 있다. 예약자가 누구인지는 이 목록 말고 어디에도 내보내지 않는다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductReservationService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserBlockRepository userBlockRepository;
    private final ChatTradeNoticeService chatTradeNoticeService;

    @Transactional(readOnly = true)
    public List<ChatPartnerDto> listChatPartners(Long productId, String sellerEmail) {
        Product product = findProduct(productId);
        Long sellerId = requireSeller(product, sellerEmail);
        return findPartners(productId, sellerId);
    }

    public List<ChatSystemNotice> reserve(Long productId, Long buyerId, String sellerEmail) {
        Product product = findProduct(productId);
        Long sellerId = requireSeller(product, sellerEmail);
        if (product.getProductType() != ProductType.SELL || product.getTradeStatus() != TradeStatus.SELLING) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_ALLOWED);
        }
        boolean isPartner = findPartners(productId, sellerId).stream().anyMatch(p -> p.userId().equals(buyerId));
        if (!isPartner) {
            throw new BusinessException(ErrorCode.RESERVATION_BUYER_NOT_PARTNER);
        }
        product.reserveFor(buyerId);
        productRepository.save(product);
        return chatTradeNoticeService.post(productId, sellerId, buyerId,
                "예약됐어요. 여기서 결제할 수 있어요", "판매자가 다른 분과 예약해서 대화가 잠시 멈췄어요");
    }

    /**
     * 이 상품 방의 구매자 중, 방에 남아 있고 판매자가 차단하지 않았고 탈퇴하지 않은 사람. 최근 대화 순.
     */
    private List<ChatPartnerDto> findPartners(Long productId, Long sellerId) {
        return chatRoomRepository.findAllByProductId(productId).stream()
                .map(ChatRoom::getId)
                .map(roomId -> chatRoomUserRepository.findByChatRoomId(roomId).stream()
                        .filter(u -> !u.getUserId().equals(sellerId))
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .filter(u -> !userBlockRepository.existsByBlockerIdAndBlockedUserId(sellerId, u.getUserId()))
                .filter(u -> userRepository.findByIdAndDeletedAtIsNull(u.getUserId()).isPresent())
                .sorted(Comparator.comparing(ChatRoomUser::getLastMessageAt,
                        Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
                .map(u -> new ChatPartnerDto(u.getUserId(), u.getUserNickname(), u.getUserProfileImageUrl(),
                        u.getLastMessageAt()))
                .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ProductNotFoundException("상품을 찾을 수 없습니다."));
    }

    private Long requireSeller(Product product, String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        if (!product.getSellerId().equals(user.getId())) {
            throw new AuthenticationFailedException("예약할 권한이 없습니다.");
        }
        return user.getId();
    }
}
