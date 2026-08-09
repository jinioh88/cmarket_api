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
    
    /**
     * WebSocket Heartbeat를 위한 TaskScheduler
     * 
     * @Primary를 사용하여 여러 TaskScheduler 중 기본으로 사용되도록 설정
     */
    @Bean(name = "websocketTaskScheduler")
    @org.springframework.context.annotation.Primary
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
        // ① 순수 WebSocket - 앱(React Native)용
        //
        // 앱은 SockJS를 못 쓴다. sockjs-client가 XHR·쿠키 같은 브라우저 API를 전제하기 때문이다.
        // SockJS 엔드포인트의 /websocket 경로도 SockJS 프레이밍을 기대해서, 순수 STOMP
        // 프레임을 보내면 서버가 응답하지 않는다(2026-08-10 실측 확인).
        //
        // 출처는 SockJS 쪽과 같은 목록을 쓴다. * 로 열지 않는다.
        //
        // Spring은 Origin 헤더가 없는 요청(앱 같은 비브라우저)은 검사 대상으로 안 본다.
        // 앱은 브라우저가 아니라 Origin을 안 보내므로 이 목록 그대로도 붙는다 (2026-08-10 실측):
        //   Origin 없음          -> 열림  (앱)
        //   evil.example.com     -> 403   (검사가 실제로 돈다)
        //   *.vercel.app         -> 열림
        // * 로 열면 저 403이 통과하게 된다. 토큰이 없어 CONNECT는 어차피 막히지만,
        // 막을 수 있는 문을 굳이 열어 둘 이유가 없다.
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(
                        "http://localhost:*",
                        "http://10.212.102.5:3000",
                        "https://localhost:*",
                        "https://*.vercel.app"
                );

        // ② SockJS - 웹용 (기존)
        //
        // 같은 경로에 둘을 등록할 수 있다. 웹은 지금처럼 SockJS로 붙고, 앱은 ①로 붙는다.
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns(
                        "http://localhost:*",  // 로컬 개발 환경
                        "http://10.212.102.5:3000",  // 내부망 개발 환경
                        "https://localhost:*",  // 로컬 개발 환경 (HTTPS)
                        "https://*.vercel.app"  // Vercel 모든 서브도메인 (프리뷰 + 프로덕션)
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
