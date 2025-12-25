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
        
        try {
            switch (command) {
                case CONNECT:
                    if (!handleConnect(accessor)) {
                        // 인증 실패 시 메시지 차단 (null 반환)
                        return null;
                    }
                    break;
                case SUBSCRIBE:
                    if (!handleSubscribe(accessor)) {
                        // 권한 없음 시 메시지 차단
                        return null;
                    }
                    break;
                case SEND:
                    if (!handleSend(accessor, message)) {
                        // 권한 없음 시 메시지 차단
                        return null;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            // 예외 발생 시 로깅하고 메시지 차단
            log.error("STOMP 메시지 처리 중 오류 발생: command={}, error={}", command, e.getMessage(), e);
            sendErrorToUserOrSession(accessor, "INTERNAL_ERROR", "메시지 처리 중 오류가 발생했습니다.");
            return null;
        }
        
        return message;
    }
    
    /**
     * CONNECT 명령어 처리 - JWT 인증
     * 
     * @return 인증 성공 시 true, 실패 시 false
     */
    private boolean handleConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        
        if (token == null || token.isBlank()) {
            log.warn("WebSocket 연결 실패: Authorization 헤더 없음");
            sendErrorToUserOrSession(accessor, "AUTH_REQUIRED", "인증 토큰이 필요합니다.");
            return false;
        }
        
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("WebSocket 연결 실패: 유효하지 않은 토큰");
            sendErrorToUserOrSession(accessor, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            return false;
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
        return true;
    }
    
    /**
     * SUBSCRIBE 명령어 처리 - 구독 권한 확인
     * 
     * 채팅방 구독 시 해당 채팅방의 참여자인지 확인합니다.
     * 구독 경로: /topic/chat/{chatRoomId}
     * 
     * @return 권한 확인 성공 시 true, 실패 시 false
     */
    private boolean handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        
        if (destination == null || user == null) {
            // destination이나 user가 null이면 기본적으로 허용 (다른 구독일 수 있음)
            return true;
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
                    return false;
                }
                
                log.debug("채팅방 구독: chatRoomId={}, email={}", chatRoomId, email);
                
            } catch (NumberFormatException e) {
                log.warn("잘못된 채팅방 ID: {}", chatRoomIdStr);
                sendErrorToUser(accessor, "INVALID_CHAT_ROOM", "잘못된 채팅방 ID입니다.");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * SEND 명령어 처리 - 전송 권한 확인
     * 
     * 메시지 전송 시 해당 채팅방의 참여자인지 확인합니다.
     * 
     * @return 권한 확인 성공 시 true, 실패 시 false
     */
    private boolean handleSend(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        Principal user = accessor.getUser();
        
        if (destination == null || user == null) {
            // destination이나 user가 null이면 기본적으로 허용 (다른 메시지일 수 있음)
            return true;
        }
        
        // 채팅 메시지 전송인 경우
        if (destination.equals("/app/chat/message")) {
            String email = user.getName();
            
            try {
                // 메시지 페이로드에서 chatRoomId 추출
                ChatMessageRequest request = parsePayload(message.getPayload());
                
                // 디버깅: 인터셉터에서 메시지 수신 로그
                String requestContent = request != null ? request.getContent() : null;
                int contentLength = requestContent != null ? requestContent.length() : 0;
                log.info("=== StompChannelInterceptor.handleSend === email={}, chatRoomId={}, content=[{}], contentLength={}", 
                        email, request != null ? request.getChatRoomId() : null,
                        requestContent, contentLength);
                
                // 경고: 짧은 메시지(1-2자)는 프론트엔드에서 메시지가 분할되어 전송되었을 가능성이 있음
                if (requestContent != null && contentLength <= 2) {
                    log.warn("⚠️ [인터셉터] 짧은 메시지 감지: content=[{}], length={}. 프론트엔드에서 메시지가 분할되어 전송되었을 가능성이 있습니다.", 
                            requestContent, contentLength);
                }
                
                if (request == null || request.getChatRoomId() == null) {
                    log.warn("메시지 페이로드 파싱 실패");
                    sendErrorToUser(accessor, "INVALID_MESSAGE", "잘못된 메시지 형식입니다.");
                    return false;
                }
                
                Long chatRoomId = request.getChatRoomId();
                
                // 채팅방 참여자인지 확인
                if (!chatService.isParticipant(chatRoomId, email)) {
                    log.warn("메시지 전송 권한 없음: chatRoomId={}, email={}", chatRoomId, email);
                    sendErrorToUser(accessor, "ACCESS_DENIED", "해당 채팅방에 메시지를 보낼 권한이 없습니다.");
                    return false;
                }
                
                log.info("=== StompChannelInterceptor 권한 확인 완료 === chatRoomId={}, email={}", chatRoomId, email);
                
            } catch (Exception e) {
                log.error("메시지 전송 권한 확인 실패", e);
                sendErrorToUser(accessor, "INTERNAL_ERROR", "메시지 처리 중 오류가 발생했습니다.");
                return false;
            }
        }
        
        return true;
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
    
    /**
     * 사용자 또는 세션에 에러 메시지 전송
     * 
     * CONNECT 단계에서는 user가 null일 수 있으므로,
     * 세션 ID를 사용하여 에러를 전송합니다.
     */
    private void sendErrorToUserOrSession(StompHeaderAccessor accessor, String errorCode, String errorMessage) {
        Principal user = accessor.getUser();
        
        if (user != null) {
            // user가 있으면 기존 방식 사용
            sendErrorToUser(accessor, errorCode, errorMessage);
        } else {
            // user가 없으면 (CONNECT 단계) 세션 ID를 사용하여 에러 전송
            String sessionId = accessor.getSessionId();
            if (sessionId != null) {
                Map<String, Object> errorPayload = Map.of(
                        "code", errorCode,
                        "message", errorMessage,
                        "timestamp", System.currentTimeMillis()
                );
                
                // 세션 ID를 사용하여 에러 전송
                messagingTemplate.convertAndSend("/queue/errors/" + sessionId, errorPayload);
            } else {
                log.warn("에러 메시지 전송 실패: user와 sessionId 모두 null");
            }
        }
    }
}
