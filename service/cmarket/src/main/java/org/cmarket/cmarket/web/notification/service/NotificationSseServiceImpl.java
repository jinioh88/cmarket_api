package org.cmarket.cmarket.web.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationDto;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 연결 관리 서비스 구현체
 * 
 * NotificationSender 인터페이스의 SSE 구현체입니다.
 * 
 * 주요 기능:
 * - SSE 연결 생성 및 관리
 * - 실시간 알림 전송
 * - Heartbeat를 통한 연결 유지 (15초마다 ping 전송)
 */
@Slf4j
@Service
public class NotificationSseServiceImpl implements NotificationSseService {
    
    private final ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    // Heartbeat 간격: 15초
    private static final long HEARTBEAT_INTERVAL_MS = 15000L;
    
    @Override
    public SseEmitter connect(Long userId) {
        // 기존 연결이 있으면 제거
        SseEmitter existingEmitter = emitters.remove(userId);
        if (existingEmitter != null) {
            try {
                existingEmitter.complete();
            } catch (Exception e) {
                log.warn("기존 SSE 연결 정리 실패: userId={}", userId, e);
            }
        }
        
        // 새로운 SseEmitter 생성 (timeout: 30초)
        // 단일 서버 환경에서 Nginx 프록시 타임아웃과 겹치지 않도록 30초로 설정
        SseEmitter emitter = new SseEmitter(30000L);
        emitters.put(userId, emitter);
        
        // 연결 해제 시 정리 작업
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.debug("SSE 연결 완료: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.debug("SSE 연결 타임아웃: userId={}", userId);
        });
        emitter.onError((ex) -> {
            emitters.remove(userId);
            log.error("SSE 연결 오류: userId={}", userId, ex);
        });
        
        // 연결 직후 "Connect Success" 더미 데이터 전송 (503 에러 방지)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connect Success"));
        } catch (Exception e) {
            log.error("SSE 연결 초기 데이터 전송 실패: userId={}", userId, e);
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    @Override
    public void sendNotification(Long userId, NotificationDto notificationDto) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.debug("SSE 연결이 없어 알림 전송 불가: userId={}", userId);
            return;
        }
        
        try {
            // MediaType.APPLICATION_JSON 명시하여 안전한 시리얼라이제이션 보장
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notificationDto, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.error("알림 전송 실패: userId={}, error={}", userId, e.getMessage(), e);
            emitters.remove(userId);
            // 클라이언트에게 명확한 에러 상태 전달
            try {
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.warn("SSE 에러 상태 전달 실패: userId={}", userId, ex);
            }
        }
    }
    
    @Override
    public void disconnect(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }
    
    @Override
    public boolean isConnected(Long userId) {
        return emitters.containsKey(userId);
    }
    
    /**
     * Heartbeat 전송
     * 
     * 네트워크 장비(공유기, 방화벽)에 의해 연결이 끊기는 것을 방지하기 위해
     * 주기적으로 빈 데이터(ping)를 전송합니다.
     * 
     * 15초마다 실행됩니다.
     */
    @Scheduled(fixedDelay = HEARTBEAT_INTERVAL_MS)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        
        // 모든 연결된 사용자에게 ping 전송
        Map<Long, SseEmitter> emittersSnapshot = new ConcurrentHashMap<>(emitters);
        
        for (Map.Entry<Long, SseEmitter> entry : emittersSnapshot.entrySet()) {
            Long userId = entry.getKey();
            SseEmitter emitter = entry.getValue();
            
            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("ping"));
            } catch (Exception e) {
                log.debug("Heartbeat 전송 실패 (연결 해제됨): userId={}", userId);
                emitters.remove(userId);
                try {
                    emitter.complete();
                } catch (Exception ex) {
                    // 이미 완료된 경우 무시
                }
            }
        }
    }
}
