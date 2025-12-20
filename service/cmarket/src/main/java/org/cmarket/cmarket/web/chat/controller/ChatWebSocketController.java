package org.cmarket.cmarket.web.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageDto;
import org.cmarket.cmarket.domain.chat.app.dto.ChatRoomListItemDto;
import org.cmarket.cmarket.domain.chat.app.service.ChatService;
import org.cmarket.cmarket.web.chat.dto.ChatMessageRequest;
import org.cmarket.cmarket.web.chat.dto.ChatMessageResponse;
import org.cmarket.cmarket.web.chat.dto.ChatRoomListItemResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket 채팅 메시지 컨트롤러
 * 
 * STOMP 프로토콜을 사용한 실시간 채팅 메시지 처리를 담당합니다.
 * 
 * 구독 경로:
 * - /topic/chat/{chatRoomId}: 채팅방 메시지 구독 (일반 메시지)
 * - /user/queue/chat: 개인 메시지 (차단된 메시지 - 발신자 전용)
 * - /user/queue/chat-room-list: 채팅방 목록 업데이트 이벤트 (실시간 목록 갱신용)
 * - /user/queue/errors: 에러 메시지
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 채팅 메시지 전송
     * 
     * 클라이언트에서 /app/chat/message로 전송하면 처리됩니다.
     * 
     * @param request 메시지 요청
     * @param principal 인증된 사용자 정보
     * @param headerAccessor STOMP 헤더
     */
    @MessageMapping("/chat/message")
    public void sendMessage(
            @Payload ChatMessageRequest request,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        if (principal == null) {
            log.warn("인증되지 않은 사용자의 메시지 전송 시도");
            return;
        }
        
        String email = principal.getName();
        Long chatRoomId = request.getChatRoomId();
        String content = request.getContent();
        
        // 경고: 짧은 메시지(1-2자)는 프론트엔드에서 메시지가 분할되어 전송되었을 가능성이 있음
        if (content != null && content.length() <= 2) {
            log.warn("⚠️ 짧은 메시지 감지: content=[{}], length={}. 프론트엔드에서 메시지가 분할되어 전송되었을 가능성이 있습니다. " +
                    "프론트엔드 개발자에게 확인이 필요합니다.", content, content.length());
        }
        
        try {
            // 1. 메시지 저장 및 처리
            ChatMessageCommand command = ChatMessageCommand.builder()
                    .chatRoomId(chatRoomId)
                    .content(request.getContent())
                    .messageType(request.getMessageType())
                    .imageUrl(request.getImageUrl())
                    .build();
            
            ChatMessageDto messageDto = chatService.sendMessage(email, command);

            ChatMessageResponse response = ChatMessageResponse.from(messageDto);
            
            // 2. 메시지 전송
            if (Boolean.TRUE.equals(response.getIsBlocked())) {
                // 개인정보 차단 메시지: 발신자에게만 전송
                messagingTemplate.convertAndSendToUser(
                        email,
                        "/queue/chat",
                        response
                );
                log.info("차단된 메시지 발신자에게만 전송: chatRoomId={}, senderId={}, reason={}", 
                        chatRoomId, messageDto.getSenderId(), response.getBlockReason());
            } else {
                // 일반 메시지: 채팅방 구독자 전체에게 전송
                messagingTemplate.convertAndSend(
                        "/topic/chat/" + chatRoomId,
                        response
                );
            }
            
            // 3. 채팅방 목록 실시간 업데이트 이벤트 전송
            // 메시지가 전송되면 채팅방 목록 화면에 있는 모든 참여자에게 업데이트 이벤트 전송
            sendChatRoomListUpdate(chatRoomId);
            
        } catch (Exception e) {
            log.error("메시지 전송 실패: chatRoomId={}, email={}", chatRoomId, email, e);
            // 발신자에게 에러 메시지 전송
            messagingTemplate.convertAndSendToUser(
                    email,
                    "/queue/errors",
                    new ErrorMessage("MESSAGE_SEND_FAILED", e.getMessage())
            );
        }
    }
    
    /**
     * 채팅방 목록 업데이트 이벤트 전송
     * 
     * 메시지가 전송되면 채팅방 목록 화면에 있는 모든 참여자에게 업데이트 이벤트를 전송합니다.
     * 프론트엔드는 /user/queue/chat-room-list를 구독하여 실시간으로 목록을 업데이트할 수 있습니다.
     * 
     * @param chatRoomId 채팅방 ID
     */
    private void sendChatRoomListUpdate(Long chatRoomId) {
        try {
            // 1. 채팅방의 모든 활성 참여자 이메일 조회
            java.util.List<String> participantEmails = chatService.getActiveParticipantEmails(chatRoomId);
            
            // 2. 각 참여자에게 채팅방 목록 아이템 전송
            for (String participantEmail : participantEmails) {
                ChatRoomListItemDto listItem = chatService.getChatRoomListItem(participantEmail, chatRoomId);
                
                if (listItem != null) {
                    ChatRoomListItemResponse updateEvent = ChatRoomListItemResponse.fromDto(listItem);
                    
                    // 개인 큐로 전송 (각 사용자에게만 전달됨)
                    messagingTemplate.convertAndSendToUser(
                            participantEmail,
                            "/queue/chat-room-list",
                            updateEvent
                    );
                }
            }
            
            log.debug("채팅방 목록 업데이트 이벤트 전송 완료: chatRoomId={}, participantCount={}", 
                    chatRoomId, participantEmails.size());
            
        } catch (Exception e) {
            // 목록 업데이트 실패는 메시지 전송에 영향을 주지 않도록 로그만 남김
            log.warn("채팅방 목록 업데이트 이벤트 전송 실패: chatRoomId={}", chatRoomId, e);
        }
    }
    
    /**
     * 시스템 메시지 전송 (채팅방 나가기 등)
     * 
     * @param chatRoomId 채팅방 ID
     * @param content 시스템 메시지 내용
     */
    public void sendSystemMessage(Long chatRoomId, String content) {
        ChatMessageResponse systemMessage = ChatMessageResponse.builder()
                .chatRoomId(chatRoomId)
                .messageType(org.cmarket.cmarket.domain.chat.model.MessageType.SYSTEM)
                .content(content)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatRoomId,
                systemMessage
        );
        
        log.info("시스템 메시지 전송: chatRoomId={}, content={}", chatRoomId, content);
    }
    
    /**
     * 에러 메시지 DTO
     */
    public record ErrorMessage(String code, String message) {}
}
