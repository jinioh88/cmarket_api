package org.cmarket.cmarket.web.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.chat.app.service.ChatSessionService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 이벤트 리스너
 * 
 * WebSocket 연결/해제 이벤트를 처리합니다.
 * 
 * 이벤트:
 * - SessionConnectedEvent: 연결 완료 시 사용자 세션 정보 저장 (Redis)
 * - SessionDisconnectEvent: 연결 해제 시 사용자 세션 정보 제거 (Redis)
 * 
 * sessionId → userId 매핑을 메모리에 캐시하여 Disconnect 이벤트 처리 시 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    
    private final ChatSessionService chatSessionService;
    private final UserRepository userRepository;
    
    // sessionId → userId 매핑 (Disconnect 이벤트에서 userId를 찾기 위해)
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    
    /**
     * WebSocket 연결 완료 이벤트 처리
     * 
     * 연결 완료 시 사용자 세션 정보를 Redis에 저장합니다.
     * 이를 통해 사용자의 온라인 상태를 추적할 수 있습니다.
     * 
     * @param event 연결 완료 이벤트
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();
        
        if (user != null && sessionId != null) {
            String email = user.getName();
            
            // 이메일로 userId 조회
            Optional<User> userOptional = userRepository.findByEmailAndDeletedAtIsNull(email);
            
            if (userOptional.isPresent()) {
                Long userId = userOptional.get().getId();
                
                // 세션 등록
                chatSessionService.addUserSession(userId, sessionId);
                
                // sessionId → userId 매핑 저장 (Disconnect 시 사용)
                sessionUserMap.put(sessionId, userId);
                
                log.debug("WebSocket 연결: sessionId={}, email={}, userId={}", sessionId, email, userId);
            }
        }
    }
    
    /**
     * WebSocket 연결 해제 이벤트 처리
     * 
     * 연결 해제 시 사용자 세션 정보를 Redis에서 제거합니다.
     * 사용자의 현재 채팅방 정보도 함께 제거됩니다.
     * 
     * @param event 연결 해제 이벤트
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        if (sessionId != null) {
            // 캐시된 userId 조회
            Long userId = sessionUserMap.remove(sessionId);
            
            if (userId != null) {
                // 세션 제거
                chatSessionService.removeUserSession(userId, sessionId);
                
                log.debug("WebSocket 해제: sessionId={}, userId={}", sessionId, userId);
            }
        }
    }
}
