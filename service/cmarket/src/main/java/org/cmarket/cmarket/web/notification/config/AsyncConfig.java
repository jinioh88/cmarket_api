package org.cmarket.cmarket.web.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 * 
 * 알림 생성 및 전송을 위한 비동기 쓰레드 풀을 설정합니다.
 * 
 * 설정:
 * - corePoolSize: 5 (기본 쓰레드 수)
 * - maxPoolSize: 10 (최대 쓰레드 수)
 * - queueCapacity: 100 (대기 큐 크기)
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * 알림 전송 전용 쓰레드 풀 빈 생성
     * 
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-async-");
        executor.initialize();
        return executor;
    }
}
