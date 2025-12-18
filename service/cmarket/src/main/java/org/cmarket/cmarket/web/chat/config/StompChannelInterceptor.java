package org.cmarket.cmarket.web.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.chat.app.service.ChatService;
import org.cmarket.cmarket.web.chat.dto.ChatMessageRequest;
import org.cmarket.cmarket.web.common.security.JwtTokenProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

/**
 * STOMP 채널 인터셉터
 * 
 * WebSocket 메시지를 가로채서 인증/인가 처리를 수행합니다.
 * 
 * 처리 항목:
 * 1. CONNECT: JWT 토큰 검증 및 인증 정보 설정
 * 2. SUBSCRIBE: 채팅방 구독 권한 확인 (참여자 여부)
 * 3. SEND: 메시지 전송 권한 확인
 * 
 * 에러 발생 시 /user/queue/errors로 에러 메시지를 전송합니다.
 * 
 * 클라이언트 연결 예시:
 * ```javascript
 * const socket = new SockJS('/ws-stomp');
 * const stompClient = Stomp.over(socket);
 * 
 * // 에러 구독 (연결 후)
 * stompClient.subscribe('/user/queue/errors', function(error) {
 *     console.error('에러:', JSON.parse(error.body));
 * });
 * 
 * stompClient.connect(
 *     { Authorization: 'Bearer {accessToken}' },
 *     onConnected,
 *     onError
 * );
 * ```
 */
@Slf4j
@Component
public class StompChannelInterceptor implements ChannelInterceptor {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    
    public StompChannelInterceptor(
            JwtTokenProvider jwtTokenProvider,
            @Lazy SimpMessagingTemplate messagingTemplate,
            ChatService chatService,
            ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, 
                StompHeaderAccessor.class
        );
        
        if (accessor == null) {
            return message;
        }
        
        StompCommand command = accessor.getCommand();
        
        if (command == null) {
            return message;
        }
        
        switch (command) {
            case CONNECT:
                handleConnect(accessor);
                break;
            case SUBSCRIBE:
                handleSubscribe(accessor);
                break;
            case SEND:
                handleSend(accessor, message);
                break;
            default:
                break;
        }
        
        return message;
    }
    
    /**
     * CONNECT 명령어 처리 - JWT 인증
     */
    private void handleConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        
        if (token == null || token.isBlank()) {
            log.warn("WebSocket 연결 실패: Authorization 헤더 없음");
            sendErrorToUser(accessor, "AUTH_REQUIRED", "인증 토큰이 필요합니다.");
            throw new SecurityException("인증 토큰이 필요합니다.");
        }
        
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("WebSocket 연결 실패: 유효하지 않은 토큰");
            sendErrorToUser(accessor, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            throw new SecurityException("유효하지 않은 토큰입니다.");
        }
        
        // 인증 정보 설정
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        accessor.setUser(authentication);
        
        // 사용자 이메일을 세션 속성에 저장 (후속 처리에서 사용)
        String email = jwtTokenProvider.getEmailFromToken(token);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("email", email);
        }
        
        log.info("WebSocket 연결 성공: email={}", email);
    }
    
    /**
     * SUBSCRIBE 명령어 처리 - 구독 권한 확인
     * 
     * 채팅방 구독 시 해당 채팅방의 참여자인지 확인합니다.
     * 구독 경로: /topic/chat/{chatRoomId}
     */
    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        
        if (destination == null || user == null) {
            return;
        }
        
        // 채팅방 구독인 경우 권한 확인
        if (destination.startsWith("/topic/chat/")) {
            String chatRoomIdStr = destination.replace("/topic/chat/", "");
            
            try {
                Long chatRoomId = Long.parseLong(chatRoomIdStr);
                String email = user.getName();
                
                // 채팅방 참여자인지 확인
                if (!chatService.isParticipant(chatRoomId, email)) {
                    log.warn("채팅방 구독 권한 없음: chatRoomId={}, email={}", chatRoomId, email);
                    sendErrorToUser(accessor, "ACCESS_DENIED", "해당 채팅방에 대한 접근 권한이 없습니다.");
                    throw new SecurityException("채팅방 접근 권한이 없습니다.");
                }
                
                log.debug("채팅방 구독: chatRoomId={}, email={}", chatRoomId, email);
                
            } catch (NumberFormatException e) {
                log.warn("잘못된 채팅방 ID: {}", chatRoomIdStr);
                sendErrorToUser(accessor, "INVALID_CHAT_ROOM", "잘못된 채팅방 ID입니다.");
                throw new SecurityException("잘못된 채팅방 ID입니다.");
            }
        }
    }
    
    /**
     * SEND 명령어 처리 - 전송 권한 확인
     * 
     * 메시지 전송 시 해당 채팅방의 참여자인지 확인합니다.
     */
    private void handleSend(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        
        if (destination == null || user == null) {
            return;
        }
        
        // 채팅 메시지 전송인 경우
        if (destination.equals("/app/chat/message")) {
            String email = user.getName();
            
            try {
                // 메시지 페이로드에서 chatRoomId 추출
                ChatMessageRequest request = parsePayload(message.getPayload());
                
                if (request == null || request.getChatRoomId() == null) {
                    log.warn("메시지 페이로드 파싱 실패");
                    sendErrorToUser(accessor, "INVALID_MESSAGE", "잘못된 메시지 형식입니다.");
                    throw new SecurityException("잘못된 메시지 형식입니다.");
                }
                
                Long chatRoomId = request.getChatRoomId();
                
                // 채팅방 참여자인지 확인
                if (!chatService.isParticipant(chatRoomId, email)) {
                    log.warn("메시지 전송 권한 없음: chatRoomId={}, email={}", chatRoomId, email);
                    sendErrorToUser(accessor, "ACCESS_DENIED", "해당 채팅방에 메시지를 보낼 권한이 없습니다.");
                    throw new SecurityException("메시지 전송 권한이 없습니다.");
                }
                
                log.debug("메시지 전송 권한 확인: chatRoomId={}, email={}", chatRoomId, email);
                
            } catch (SecurityException e) {
                throw e;
            } catch (Exception e) {
                log.error("메시지 전송 권한 확인 실패", e);
                // 권한 확인 실패 시에도 메시지는 컨트롤러에서 처리
            }
        }
    }
    
    /**
     * 메시지 페이로드 파싱
     */
    private ChatMessageRequest parsePayload(Object payload) {
        try {
            if (payload instanceof byte[]) {
                String json = new String((byte[]) payload, StandardCharsets.UTF_8);
                return objectMapper.readValue(json, ChatMessageRequest.class);
            } else if (payload instanceof String) {
                return objectMapper.readValue((String) payload, ChatMessageRequest.class);
            }
        } catch (Exception e) {
            log.debug("페이로드 파싱 실패: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Authorization 헤더에서 JWT 토큰 추출
     */
    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    /**
     * 사용자에게 에러 메시지 전송
     * 
     * /user/queue/errors 경로로 에러 정보를 전송합니다.
     * 클라이언트는 이 경로를 구독하여 에러를 수신할 수 있습니다.
     */
    private void sendErrorToUser(StompHeaderAccessor accessor, String errorCode, String errorMessage) {
        Principal user = accessor.getUser();
        
        if (user != null) {
            Map<String, Object> errorPayload = Map.of(
                    "code", errorCode,
                    "message", errorMessage,
                    "timestamp", System.currentTimeMillis()
            );
            
            messagingTemplate.convertAndSendToUser(
                    user.getName(),
                    "/queue/errors",
                    errorPayload
            );
        }
    }
}
