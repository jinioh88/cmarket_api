package org.cmarket.cmarket.web.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 메일 발송 전용 비동기 쓰레드 풀 (#849)
 *
 * ⚠️ **왜 알림 풀(notificationTaskExecutor)을 같이 쓰지 않는가**
 *
 * SMTP 왕복은 느리다(수백 ms ~ 수 초). 알림 풀은 큐가 100칸인데 메일이 그것을 먹으면
 * 알림이 밀린다. 풀 이름도 「notification-async-」라 로그에서 메일인지 알림인지 안 갈린다.
 *
 * ⚠️ **왜 메일을 비동기로 돌려야 하는가 — 이것이 없으면 계정 열거가 뚫린다**
 *
 * 계정 찾기는 「가입된 계정이 있다면 안내 메일을 보냈습니다」를 **누구에게나 똑같이** 준다.
 * 그런데 메일 발송이 요청 스레드에 있으면 **회원일 때만 SMTP 왕복만큼 느려진다** —
 * 화면 문구는 같아도 **걸리는 시간이 다르면** 그것으로 회원 여부를 알아낼 수 있다.
 *
 * ⚠️ **@EnableAsync 는 여기 안 붙인다.** 이미 AsyncConfig(web/notification/config)에 있고,
 *    애플리케이션에 하나면 된다.
 *
 * ⚠️ **이 빈이 생기면서 Executor 빈이 둘이 됐다.** 그래서 이름 없는 `@Async` 는 더 이상
 *    「유일한 Executor」를 못 찾아 SimpleAsyncTaskExecutor(요청마다 새 스레드, 상한 없음)로
 *    물러선다. 오류가 안 나고 조용히 바뀐다 — 그래서 알림 쪽 `@Async` 두 곳에
 *    `@Async("notificationTaskExecutor")` 로 이름을 박아 뒀다. **떼지 말 것.**
 *      web/notification/service/NotificationEventListener.java
 *      domain/notification/app/service/NotificationServiceImpl.java
 */
@Configuration
public class MailAsyncConfig {

    /**
     * 메일 발송 전용 쓰레드 풀 빈 생성
     *
     * 알림 풀보다 작게 잡았다 — 메일은 가입·비밀번호 찾기·계정 찾기에서만 나가고
     * 알림처럼 쏟아지지 않는다.
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("mail-async-");
        executor.initialize();
        return executor;
    }
}
