package org.cmarket.cmarket.domain.chat.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageListDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageListItemDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomCreateCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListItemDto;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationCreateCommand;
import org.cmarket.cmarket.domain.notification.app.event.NotificationCreatedEvent;
import org.cmarket.cmarket.domain.notification.model.NotificationType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.cmarket.cmarket.domain.chat.model.ChatMessage;
import org.cmarket.cmarket.domain.chat.model.ChatRoom;
import org.cmarket.cmarket.domain.chat.model.ChatRoomUser;
import org.cmarket.cmarket.domain.chat.model.MessageType;
import org.cmarket.cmarket.domain.chat.repository.ChatMessageRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomRepository;
import org.cmarket.cmarket.domain.chat.repository.ChatRoomUserRepository;
import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 채팅 서비스 구현체
 * 
 * 채팅 관련 비즈니스 로직을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ChatReadStatusService chatReadStatusService;
    private final ChatSessionService chatSessionService;
    private final PrivacyFilterService privacyFilterService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public ChatRoomDto createChatRoom(String email, ChatRoomCreateCommand command) {
        // 1. 현재 로그인한 사용자(구매자) 조회
        User buyer = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 2. 상품 존재 여부 확인
        Product product = productRepository.findByIdAndDeletedAtIsNull(command.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        
        // 3. 판매자 조회
        User seller = userRepository.findById(product.getSellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 4. 판매자 본인과의 채팅 방지
        if (buyer.getId().equals(seller.getId())) {
            throw new BusinessException(ErrorCode.SELF_CHAT_NOT_ALLOWED);
        }
        
        // 5. 기존 채팅방 존재 확인 (동일 상품 + 구매자 + 판매자)
        Optional<ChatRoom> existingChatRoom = findExistingChatRoom(
                command.getProductId(), 
                buyer.getId(), 
                seller.getId()
        );
        
        if (existingChatRoom.isPresent()) {
            // 기존 채팅방이 있으면 반환
            return ChatRoomDto.fromEntity(existingChatRoom.get(), seller.getNickname(), seller.getProfileImageUrl());
        }
        
        // 6. 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productPrice(product.getPrice())
                .productImageUrl(product.getMainImageUrl())
                .build();
        
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        
        // 7. ChatRoomUser 생성 (구매자)
        ChatRoomUser buyerUser = ChatRoomUser.builder()
                .chatRoomId(savedChatRoom.getId())
                .userId(buyer.getId())
                .userNickname(buyer.getNickname())
                .userProfileImageUrl(buyer.getProfileImageUrl())
                .build();
        
        chatRoomUserRepository.save(buyerUser);
        
        // 8. ChatRoomUser 생성 (판매자)
        ChatRoomUser sellerUser = ChatRoomUser.builder()
                .chatRoomId(savedChatRoom.getId())
                .userId(seller.getId())
                .userNickname(seller.getNickname())
                .userProfileImageUrl(seller.getProfileImageUrl())
                .build();
        
        chatRoomUserRepository.save(sellerUser);
        
        // 9. 알림 이벤트 발행 (상대방에게 새 채팅방 생성 알림)
        NotificationCreateCommand notificationCommand = NotificationCreateCommand.builder()
                .userId(seller.getId())  // 수신자: 판매자
                .notificationType(NotificationType.CHAT_NEW_ROOM)
                .title("새로운 채팅이 시작되었습니다")
                .content(String.format("%s님이 '%s' 상품에 대해 채팅을 시작했습니다.", buyer.getNickname(), product.getTitle()))
                .relatedEntityType("CHAT_ROOM")
                .relatedEntityId(savedChatRoom.getId())
                .build();
        
        eventPublisher.publishEvent(new NotificationCreatedEvent(this, seller.getId(), notificationCommand));
        
        return ChatRoomDto.fromEntity(savedChatRoom, seller.getNickname(), seller.getProfileImageUrl());
    }
    
    @Override
    public ChatRoomListDto getChatRoomList(String email) {
        // 1. 현재 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Long userId = user.getId();
        
        // 2. 사용자의 활성 채팅방 목록 조회 (최근 메시지 시간 기준 내림차순 - DB 레벨 정렬)
        List<ChatRoomUser> myChatRoomUsers = chatRoomUserRepository
                .findActiveByUserIdOrderByLastMessageAtDesc(userId);
        
        if (myChatRoomUsers.isEmpty()) {
            return ChatRoomListDto.builder()
                    .chatRooms(List.of())
                    .totalCount(0)
                    .build();
        }
        
        // 3. 채팅방 ID 목록 추출
        List<Long> chatRoomIds = myChatRoomUsers.stream()
                .map(ChatRoomUser::getChatRoomId)
                .toList();
        
        // 4. 채팅방 정보 일괄 조회 (IN 절 - 쿼리 1회)
        List<ChatRoom> chatRooms = chatRoomRepository.findAllById(chatRoomIds);
        java.util.Map<Long, ChatRoom> chatRoomMap = chatRooms.stream()
                .collect(java.util.stream.Collectors.toMap(ChatRoom::getId, cr -> cr));
        
        // 5. 상대방 정보 일괄 조회 (IN 절 - 쿼리 1회)
        List<ChatRoomUser> opponents = chatRoomUserRepository
                .findOpponentsByChatRoomIdsAndMyUserId(chatRoomIds, userId);
        java.util.Map<Long, ChatRoomUser> opponentMap = opponents.stream()
                .collect(java.util.stream.Collectors.toMap(ChatRoomUser::getChatRoomId, op -> op));
        
        // 6. 각 채팅방 정보 조합 (추가 쿼리 없음)
        List<ChatRoomListItemDto> chatRoomItems = new ArrayList<>();
        
        for (ChatRoomUser myChatRoomUser : myChatRoomUsers) {
            Long chatRoomId = myChatRoomUser.getChatRoomId();
            
            ChatRoom chatRoom = chatRoomMap.get(chatRoomId);
            ChatRoomUser opponent = opponentMap.get(chatRoomId);
            
            // 채팅방이 없으면 skip (데이터 이상)
            if (chatRoom == null) {
                continue;
            }
            
            // Redis에서 안 읽은 메시지 개수 조회 (Redis 호출은 빠름)
            int unreadCount = chatReadStatusService.getUnreadCount(chatRoomId, userId);
            
            // 상대방이 탈퇴한 경우 "알 수 없는 사용자"로 표시
            Long opponentId = opponent != null ? opponent.getUserId() : null;
            String opponentNickname = opponent != null ? opponent.getUserNickname() : "알 수 없는 사용자";
            String opponentProfileImageUrl = opponent != null ? opponent.getUserProfileImageUrl() : null;
            
            // 최근 메시지 정보는 ChatRoomUser에 비정규화되어 있음
            ChatRoomListItemDto item = ChatRoomListItemDto.builder()
                    .chatRoomId(chatRoomId)
                    .productId(chatRoom.getProductId())
                    .productTitle(chatRoom.getProductTitle())
                    .productPrice(chatRoom.getProductPrice())
                    .productImageUrl(chatRoom.getProductImageUrl())
                    .opponentId(opponentId)
                    .opponentNickname(opponentNickname)
                    .opponentProfileImageUrl(opponentProfileImageUrl)
                    .lastMessage(myChatRoomUser.getLastMessageContent())
                    .lastMessageTime(myChatRoomUser.getLastMessageAt())
                    .hasUnread(unreadCount > 0)
                    .unreadCount(unreadCount)
                    .build();
            
            chatRoomItems.add(item);
        }
        
        // 정렬은 DB에서 이미 완료됨 (추가 Java 정렬 불필요)
        
        return ChatRoomListDto.builder()
                .chatRooms(chatRoomItems)
                .totalCount(chatRoomItems.size())
                .build();
    }
    
    @Override
    public boolean isParticipant(Long chatRoomId, String email) {
        // 사용자 조회
        Optional<User> userOptional = userRepository.findByEmailAndDeletedAtIsNull(email);
        if (userOptional.isEmpty()) {
            return false;
        }
        
        Long userId = userOptional.get().getId();
        
        // 채팅방 참여 여부 확인 (활성 상태만)
        return chatRoomUserRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId, userId);
    }
    
    @Override
    @Transactional
    public ChatMessageDto sendMessage(String email, ChatMessageCommand command) {
        // 1. 발신자 조회
        User sender = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Long chatRoomId = command.getChatRoomId();
        Long senderId = sender.getId();
        
        // 2. 채팅방 존재 확인
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 3. 발신자 참여 정보 조회
        ChatRoomUser senderChatRoomUser = chatRoomUserRepository
                .findByChatRoomIdAndUserId(chatRoomId, senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED));
        
        // 4. 발신자가 채팅방을 나갔는지 확인
        if (senderChatRoomUser.isLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_USER_LEFT);
        }
        
        // 5. 상대방 조회
        ChatRoomUser opponent = chatRoomUserRepository
                .findOpponentByChatRoomIdAndMyUserId(chatRoomId, senderId)
                .orElse(null);
        
        // 6. 상대방이 나갔는지 확인
        if (opponent != null && opponent.isLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_USER_LEFT);
        }
        
        // 7. 개인정보 필터링
        boolean isBlocked = false;
        String blockReason = null;
        String content = command.getContent();
        
        // 디버깅: 메시지 내용 로그
        log.info("=== ChatServiceImpl.sendMessage 처리 시작 === chatRoomId={}, senderId={}, content=[{}], contentLength={}, contentBytes={}", 
                chatRoomId, senderId, content, 
                content != null ? content.length() : 0,
                content != null ? java.util.Arrays.toString(content.getBytes(java.nio.charset.StandardCharsets.UTF_8)) : "null");
        
        if (content != null && privacyFilterService.containsPrivateInfo(content)) {
            isBlocked = true;
            blockReason = privacyFilterService.getBlockReason(content);
        }
        
        // 8. 메시지 저장
        LocalDateTime now = LocalDateTime.now();
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .senderNickname(sender.getNickname())
                .messageType(command.getMessageType())
                .content(content)
                .imageUrl(command.getImageUrl())
                .isBlocked(isBlocked)
                .blockReason(blockReason)
                .build();
        
        log.info("=== 메시지 저장 전 === message.content=[{}], message.contentLength={}", 
                message.getContent(), message.getContent() != null ? message.getContent().length() : 0);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        log.info("=== 메시지 저장 후 === savedMessage.id={}, savedMessage.content=[{}], savedMessage.contentLength={}", 
                savedMessage.getId(), savedMessage.getContent(), 
                savedMessage.getContent() != null ? savedMessage.getContent().length() : 0);
        
        // 9. ChatRoomUser의 lastMessage 정보 업데이트 (양쪽 모두)
        senderChatRoomUser.updateLastMessage(content, now, isBlocked);
        if (opponent != null) {
            opponent.updateLastMessage(content, now, isBlocked);
        }
        
        // 10. Redis에 상대방의 안 읽은 메시지 개수 증가
        // 단, 개인정보가 포함된 메시지는 발신자에게만 표시되므로 증가하지 않음
        if (!isBlocked && opponent != null) {
            Long opponentId = opponent.getUserId();
            // 상대방이 해당 채팅방에 접속 중이면 증가하지 않음
            Long currentChatRoom = chatSessionService.getUserCurrentChatRoom(opponentId);
            if (currentChatRoom == null || !currentChatRoom.equals(chatRoomId)) {
                chatReadStatusService.incrementUnreadCount(chatRoomId, opponentId);
                
                // 11. 알림 이벤트 발행 (상대방에게 새 메시지 알림)
                ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                        .orElse(null);
                String productTitle = chatRoom != null ? chatRoom.getProductTitle() : "상품";
                
                NotificationCreateCommand notificationCommand = NotificationCreateCommand.builder()
                        .userId(opponentId)  // 수신자: 상대방
                        .notificationType(NotificationType.CHAT_NEW_MESSAGE)
                        .title("새로운 메시지가 도착했습니다")
                        .content(String.format("%s님이 '%s' 상품 채팅에서 메시지를 보냈습니다.", sender.getNickname(), productTitle))
                        .relatedEntityType("CHAT_ROOM")
                        .relatedEntityId(chatRoomId)
                        .build();
                
                eventPublisher.publishEvent(new NotificationCreatedEvent(this, opponentId, notificationCommand));
            }
        }
        
        return ChatMessageDto.fromEntity(savedMessage);
    }
    
    @Override
    public boolean isParticipantByUserId(Long chatRoomId, Long userId) {
        return chatRoomUserRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId, userId);
    }
    
    @Override
    @Transactional
    public ChatMessageListDto getChatMessages(String email, Long chatRoomId, int page, int size) {
        // 1. 현재 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Long userId = user.getId();
        
        // 2. 채팅방 존재 및 참여 여부 확인 (단일 쿼리)
        if (!chatRoomUserRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId, userId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
        
        // 4. 첫 페이지 조회 시 Redis → RDB 읽음 상태 동기화
        if (page == 0) {
            chatReadStatusService.syncReadStatusToRdb(chatRoomId, userId);
            // 현재 채팅방 설정 (실시간 읽음 처리용)
            chatSessionService.setUserCurrentChatRoom(userId, chatRoomId);
        }
        
        // 5. 메시지 목록 조회 (최신순)
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ChatMessage> messagePage = chatMessageRepository
                .findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageRequest);
        
        // 6. DTO 변환 (차단된 메시지 필터링 포함)
        List<ChatMessageListItemDto> messages = messagePage.getContent().stream()
                .map(msg -> {
                    // 차단된 메시지는 발신자 본인에게만 표시
                    if (Boolean.TRUE.equals(msg.getIsBlocked()) && !msg.getSenderId().equals(userId)) {
                        return null;  // 다른 사람이 보낸 차단된 메시지는 제외
                    }
                    return ChatMessageListItemDto.fromEntity(msg, userId);
                })
                .filter(dto -> dto != null)
                .toList();
        
        // 7. 오래된순으로 정렬 (프론트엔드에서 위에서 아래로 읽기 위함)
        List<ChatMessageListItemDto> reversedMessages = new ArrayList<>(messages);
        java.util.Collections.reverse(reversedMessages);
        
        // 8. Redis에 마지막 읽은 시간 업데이트
        chatReadStatusService.updateLastReadTime(chatRoomId, userId);
        
        return ChatMessageListDto.builder()
                .messages(reversedMessages)
                .currentPage(messagePage.getNumber())
                .totalPages(messagePage.getTotalPages())
                .totalElements(messagePage.getTotalElements())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }
    
    @Override
    @Transactional
    public ChatMessageDto leaveChatRoom(String email, Long chatRoomId) {
        // 1. 현재 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Long userId = user.getId();
        
        // 2. 채팅방 참여 정보 조회
        ChatRoomUser chatRoomUser = chatRoomUserRepository
                .findByChatRoomIdAndUserId(chatRoomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED));
        
        // 3. 이미 나간 경우 확인
        if (chatRoomUser.isLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_USER_LEFT);
        }
        
        // 4. 채팅방 나가기 (소프트 삭제)
        chatRoomUser.leave();
        
        // 5. 시스템 메시지 생성
        String systemMessageContent = user.getNickname() + "님이 채팅방을 나가셨습니다.";
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .senderId(userId)
                .senderNickname(user.getNickname())
                .messageType(MessageType.SYSTEM)
                .content(systemMessageContent)
                .build();
        
        ChatMessage savedSystemMessage = chatMessageRepository.save(systemMessage);
        
        // 6. 상대방의 lastMessage 업데이트
        chatRoomUserRepository.findOpponentByChatRoomIdAndMyUserId(chatRoomId, userId)
                .ifPresent(opponent -> opponent.updateLastMessage(
                        systemMessageContent, 
                        LocalDateTime.now(), 
                        false
                ));
        
        // 7. Redis에서 해당 사용자의 읽음 정보 삭제
        chatReadStatusService.resetUnreadCount(chatRoomId, userId);
        chatSessionService.clearUserCurrentChatRoom(userId);
        
        return ChatMessageDto.fromEntity(savedSystemMessage);
    }
    
    @Override
    public ChatRoomListItemDto getChatRoomListItem(String email, Long chatRoomId) {
        // 1. 현재 사용자 조회
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Long userId = user.getId();
        
        // 2. 사용자의 채팅방 참여 정보 조회
        ChatRoomUser myChatRoomUser = chatRoomUserRepository
                .findByChatRoomIdAndUserId(chatRoomId, userId)
                .orElse(null);
        
        // 참여하지 않은 채팅방이거나 비활성 상태면 null 반환
        if (myChatRoomUser == null || !Boolean.TRUE.equals(myChatRoomUser.getIsActive())) {
            return null;
        }
        
        // 3. 채팅방 정보 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElse(null);
        
        if (chatRoom == null) {
            return null;
        }
        
        // 4. 상대방 정보 조회
        ChatRoomUser opponent = chatRoomUserRepository
                .findOpponentByChatRoomIdAndMyUserId(chatRoomId, userId)
                .orElse(null);
        
        // 5. Redis에서 안 읽은 메시지 개수 조회
        int unreadCount = chatReadStatusService.getUnreadCount(chatRoomId, userId);
        
        // 6. 상대방이 탈퇴한 경우 "알 수 없는 사용자"로 표시
        Long opponentId = opponent != null ? opponent.getUserId() : null;
        String opponentNickname = opponent != null ? opponent.getUserNickname() : "알 수 없는 사용자";
        String opponentProfileImageUrl = opponent != null ? opponent.getUserProfileImageUrl() : null;
        
        // 7. 채팅방 목록 아이템 생성
        return ChatRoomListItemDto.builder()
                .chatRoomId(chatRoomId)
                .productId(chatRoom.getProductId())
                .productTitle(chatRoom.getProductTitle())
                .productPrice(chatRoom.getProductPrice())
                .productImageUrl(chatRoom.getProductImageUrl())
                .opponentId(opponentId)
                .opponentNickname(opponentNickname)
                .opponentProfileImageUrl(opponentProfileImageUrl)
                .lastMessage(myChatRoomUser.getLastMessageContent())
                .lastMessageTime(myChatRoomUser.getLastMessageAt())
                .hasUnread(unreadCount > 0)
                .unreadCount(unreadCount)
                .build();
    }
    
    @Override
    public List<String> getActiveParticipantEmails(Long chatRoomId) {
        // 1. 채팅방의 모든 활성 참여자 조회
        List<ChatRoomUser> participants = chatRoomUserRepository.findByChatRoomId(chatRoomId)
                .stream()
                .filter(participant -> Boolean.TRUE.equals(participant.getIsActive()))
                .toList();
        
        // 2. 참여자 ID 목록 추출
        List<Long> userIds = participants.stream()
                .map(ChatRoomUser::getUserId)
                .toList();
        
        if (userIds.isEmpty()) {
            return List.of();
        }
        
        // 3. 사용자 이메일 조회
        return userRepository.findAllById(userIds).stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .toList();
    }
    
    /**
     * 기존 채팅방 조회 (상품 + 두 사용자 기준)
     * 
     * @param productId 상품 ID
     * @param buyerId 구매자 ID
     * @param sellerId 판매자 ID
     * @return 기존 채팅방 (없으면 Optional.empty())
     */
    private Optional<ChatRoom> findExistingChatRoom(Long productId, Long buyerId, Long sellerId) {
        // 1. 상품의 채팅방 조회
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findByProductId(productId);
        if (chatRoomOptional.isEmpty()) {
            return Optional.empty();
        }
        
        ChatRoom chatRoom = chatRoomOptional.get();
        
        // 2. 두 사용자가 모두 참여했는지 확인
        long participantCount = chatRoomUserRepository.countByChatRoomIdAndUserIdIn(
                chatRoom.getId(),
                List.of(buyerId, sellerId)
        );
        
        return participantCount == 2 ? Optional.of(chatRoom) : Optional.empty();
    }
}
