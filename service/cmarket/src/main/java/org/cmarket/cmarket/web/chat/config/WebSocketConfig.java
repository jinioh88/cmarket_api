package org.cmarket.cmarket.web.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 설정
 * 
 * STOMP(Simple Text Oriented Messaging Protocol)를 사용한 실시간 채팅 설정입니다.
 * 
 * 구성:
 * - 엔드포인트: /ws-stomp (SockJS 폴백 지원)
 * - Application Destination Prefix: /app (클라이언트 → 서버 메시지)
 * - Simple Broker: /topic, /queue (서버 → 클라이언트 메시지)
 *   - /topic/chat/{chatRoomId}: 채팅방 메시지 브로드캐스트
 *   - /queue/chat/{userId}: 개인 메시지 (차단 메시지 등)
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final StompChannelInterceptor stompChannelInterceptor;
    
    @Bean
    public TaskScheduler websocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple Broker 설정 (서버 → 클라이언트)
        // /topic: 1:N 브로드캐스트 (채팅방 메시지)
        // /queue: 1:1 개인 메시지 (차단 메시지 등)
        // Heartbeat: 서버→클라이언트 2분, 클라이언트→서버 2분 간격
        // 세션 TTL(5분)보다 짧게 설정하여 연결 유지 시 자동 갱신
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{120000, 120000})  // [서버→클라이언트, 클라이언트→서버] ms
                .setTaskScheduler(websocketTaskScheduler());
        
        // Application Destination Prefix 설정 (클라이언트 → 서버)
        // 클라이언트가 /app으로 시작하는 경로로 메시지를 보내면
        // @MessageMapping이 붙은 메서드에서 처리
        registry.setApplicationDestinationPrefixes("/app");
        
        // 개인 메시지를 위한 prefix 설정
        // /queue/chat/{userId}로 개인 메시지 전송 시 사용
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정
        // 클라이언트는 /ws-stomp로 연결
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(
                        "http://localhost:*",
                        "https://localhost:*"
                )
                // SockJS 폴백 지원 (WebSocket을 지원하지 않는 브라우저용)
                .withSockJS();
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // STOMP 메시지 인터셉터 등록 (JWT 인증 처리)
        registration.interceptors(stompChannelInterceptor);
    }
}
