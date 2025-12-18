package org.cmarket.cmarket.web.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cmarket.cmarket.domain.chat.app.service.ChatReadStatusService;
import org.cmarket.cmarket.domain.chat.app.service.ChatSessionService;
import org.cmarket.cmarket.domain.chat.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 채팅 읽음 상태 관리 서비스 구현체
 * 
 * Redis를 활용한 실시간 읽음 상태 관리 기능을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatReadStatusServiceImpl implements ChatReadStatusService {
    
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionService chatSessionService;
    
    @Value("${chat.redis.ttl-days:30}")
    private int ttlDays;
    
    private static final String UNREAD_KEY_PREFIX = "chat:unread:";
    private static final String LAST_READ_KEY_PREFIX = "chat:lastread:";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    @Override
    public void incrementUnreadCount(Long chatRoomId, Long recipientId) {
        // 수신자가 해당 채팅방에 접속 중이면 증가하지 않음
        Long currentChatRoom = chatSessionService.getUserCurrentChatRoom(recipientId);
        if (currentChatRoom != null && currentChatRoom.equals(chatRoomId)) {
            return;
        }
        
        String key = buildUnreadKey(chatRoomId, recipientId);
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
    }
    
    @Override
    public int getUnreadCount(Long chatRoomId, Long userId) {
        String key = buildUnreadKey(chatRoomId, userId);
        String value = stringRedisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return 0;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public void resetUnreadCount(Long chatRoomId, Long userId) {
        String key = buildUnreadKey(chatRoomId, userId);
        stringRedisTemplate.opsForValue().set(key, "0");
        stringRedisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
    }
    
    @Override
    public LocalDateTime getLastReadTime(Long chatRoomId, Long userId) {
        String key = buildLastReadKey(chatRoomId, userId);
        String value = stringRedisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(value, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public void updateLastReadTime(Long chatRoomId, Long userId) {
        String key = buildLastReadKey(chatRoomId, userId);
        String value = LocalDateTime.now().format(DATETIME_FORMATTER);
        stringRedisTemplate.opsForValue().set(key, value);
        stringRedisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
    }
    
    @Override
    @Transactional
    public void syncReadStatusToRdb(Long chatRoomId, Long userId) {
        LocalDateTime syncTime = LocalDateTime.now();
        
        try {
            // 1. Redis의 unreadCount를 0으로 리셋
            resetUnreadCount(chatRoomId, userId);
            
            // 2. 마지막 읽은 시간 업데이트
            updateLastReadTime(chatRoomId, userId);
            
        } catch (Exception e) {
            // Redis 실패 시 로그 기록 (DB 동기화는 계속 진행)
            log.warn("Redis 읽음 상태 초기화 실패: chatRoomId={}, userId={}, error={}", 
                    chatRoomId, userId, e.getMessage());
        }
        
        try {
            // 3. RDB의 메시지 isRead 일괄 업데이트
            // 본인이 받은 메시지(senderId != userId) 중 isRead = false인 것들을 true로 변경
            int updatedCount = chatMessageRepository.markMessagesAsRead(chatRoomId, userId, syncTime);
            
            if (updatedCount > 0) {
                log.debug("RDB 읽음 상태 동기화 완료: chatRoomId={}, userId={}, updatedCount={}", 
                        chatRoomId, userId, updatedCount);
            }
            
        } catch (Exception e) {
            // DB 실패 시 에러 로그 기록
            log.error("RDB 읽음 상태 동기화 실패: chatRoomId={}, userId={}, error={}", 
                    chatRoomId, userId, e.getMessage(), e);
            throw e; // @Transactional 롤백을 위해 예외 재발생
        }
    }
    
    @Override
    public void deleteReadStatus(Long chatRoomId, Long userId) {
        String unreadKey = buildUnreadKey(chatRoomId, userId);
        String lastReadKey = buildLastReadKey(chatRoomId, userId);
        
        stringRedisTemplate.delete(unreadKey);
        stringRedisTemplate.delete(lastReadKey);
    }
    
    /**
     * 안 읽은 개수 키 생성
     */
    private String buildUnreadKey(Long chatRoomId, Long userId) {
        return UNREAD_KEY_PREFIX + chatRoomId + ":" + userId;
    }
    
    /**
     * 마지막 읽은 시간 키 생성
     */
    private String buildLastReadKey(Long chatRoomId, Long userId) {
        return LAST_READ_KEY_PREFIX + chatRoomId + ":" + userId;
    }
}
