package org.cmarket.cmarket.web.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.chat.app.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 채팅 세션 관리 서비스 구현체
 * 
 * Redis를 활용한 사용자 세션 정보 관리 기능을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {
    
    private final RedisTemplate<String, String> stringRedisTemplate;
    
    @Value("${chat.redis.ttl-days:30}")
    private int ttlDays;
    
    private static final String SESSION_KEY_PREFIX = "chat:session:";
    private static final String CURRENT_ROOM_KEY_PREFIX = "chat:current:";
    
    // 세션 TTL은 5분 (짧게 유지하여 서버 재시작 시 자동 만료)
    // WebSocket 연결이 유지되는 동안 주기적으로 갱신됨
    private static final int SESSION_TTL_MINUTES = 5;
    
    @Override
    public void addUserSession(Long userId, String sessionId) {
        String key = buildSessionKey(userId);
        stringRedisTemplate.opsForValue().set(key, sessionId);
        stringRedisTemplate.expire(key, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        
        log.debug("사용자 세션 등록: userId={}, sessionId={}", userId, sessionId);
    }
    
    /**
     * 세션 TTL 갱신 (Heartbeat)
     * 
     * WebSocket 연결이 유지되는 동안 주기적으로 호출하여 세션 만료를 방지합니다.
     * 서버 재시작/크래시 시에는 갱신이 중단되어 자동 만료됩니다.
     * 
     * @param userId 사용자 ID
     */
    public void refreshSessionTtl(Long userId) {
        String sessionKey = buildSessionKey(userId);
        String currentRoomKey = buildCurrentRoomKey(userId);
        
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(sessionKey))) {
            stringRedisTemplate.expire(sessionKey, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        }
        
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(currentRoomKey))) {
            stringRedisTemplate.expire(currentRoomKey, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }
    
    @Override
    public void removeUserSession(Long userId, String sessionId) {
        String sessionKey = buildSessionKey(userId);
        String currentSessionId = stringRedisTemplate.opsForValue().get(sessionKey);
        
        // 현재 저장된 세션 ID와 일치하는 경우에만 삭제
        // (다른 기기에서 접속한 경우 해당 세션만 삭제되도록)
        if (sessionId.equals(currentSessionId)) {
            stringRedisTemplate.delete(sessionKey);
            
            // 현재 채팅방 정보도 함께 삭제
            clearUserCurrentChatRoom(userId);
            
            log.debug("사용자 세션 제거: userId={}, sessionId={}", userId, sessionId);
        }
    }
    
    @Override
    public boolean isUserOnline(Long userId) {
        String key = buildSessionKey(userId);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
    
    @Override
    public Long getUserCurrentChatRoom(Long userId) {
        String key = buildCurrentRoomKey(userId);
        String value = stringRedisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }
        
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @Override
    public void setUserCurrentChatRoom(Long userId, Long chatRoomId) {
        if (chatRoomId == null) {
            clearUserCurrentChatRoom(userId);
            return;
        }
        
        String key = buildCurrentRoomKey(userId);
        stringRedisTemplate.opsForValue().set(key, chatRoomId.toString());
        stringRedisTemplate.expire(key, SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        
        log.debug("현재 채팅방 설정: userId={}, chatRoomId={}", userId, chatRoomId);
    }
    
    @Override
    public void clearUserCurrentChatRoom(Long userId) {
        String key = buildCurrentRoomKey(userId);
        stringRedisTemplate.delete(key);
        
        log.debug("현재 채팅방 초기화: userId={}", userId);
    }
    
    /**
     * 세션 키 생성
     */
    private String buildSessionKey(Long userId) {
        return SESSION_KEY_PREFIX + userId;
    }
    
    /**
     * 현재 채팅방 키 생성
     */
    private String buildCurrentRoomKey(Long userId) {
        return CURRENT_ROOM_KEY_PREFIX + userId;
    }
}
