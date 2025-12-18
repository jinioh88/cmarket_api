package org.cmarket.cmarket.web.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 * 
 * 채팅 기능에서 사용하는 Redis 설정입니다.
 * 
 * 용도:
 * - 읽음 상태 관리 (안 읽은 메시지 개수)
 * - 사용자 세션 정보 관리 (온라인 상태)
 * - 현재 접속 중인 채팅방 정보
 * 
 * Key 구조:
 * - chat:unread:{chatRoomId}:{userId} - 안 읽은 메시지 개수
 * - chat:lastread:{chatRoomId}:{userId} - 마지막 읽은 시간
 * - chat:session:{userId} - 사용자 세션 ID
 * - chat:current:{userId} - 현재 접속 중인 채팅방 ID
 */
@Configuration
public class RedisConfig {
    
    @Value("${chat.redis.ttl-days:30}")
    private int ttlDays;
    
    /**
     * RedisTemplate 설정
     * 
     * Key: String 직렬화
     * Value: JSON 직렬화 (GenericJackson2JsonRedisSerializer)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key 직렬화: String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value 직렬화: JSON
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * String 전용 RedisTemplate
     * 
     * 단순 문자열 값 저장에 사용합니다.
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
