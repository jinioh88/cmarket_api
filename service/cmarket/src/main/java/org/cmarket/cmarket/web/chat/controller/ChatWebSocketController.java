package org.cmarket.cmarket.web.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageCommand;
import org.cmarket.cmarket.domain.chat.app.dto.ChatMessageDto;
import org.cmarket.cmarket.domain.chat.app.service.ChatService;
import org.cmarket.cmarket.web.chat.dto.ChatMessageRequest;
import org.cmarket.cmarket.web.chat.dto.ChatMessageResponse;
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
                log.debug("메시지 전송 완료: chatRoomId={}, messageId={}", 
                        chatRoomId, messageDto.getMessageId());
            }
            
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
