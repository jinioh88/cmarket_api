package org.cmarket.cmarket.domain.notification.app.event;

import lombok.Getter;
import org.cmarket.cmarket.domain.notification.app.dto.NotificationCreateCommand;
import org.springframework.context.ApplicationEvent;

/**
 * 알림 생성 요청 이벤트
 * 
 * 서비스 로직에서 알림 생성 요청을 이벤트로 발행할 때 사용합니다.
 * Spring Event를 통한 이벤트 발행/구독 구조로 서비스 로직과 알림 로직의 결합도를 낮춥니다.
 */
@Getter
public class NotificationCreatedEvent extends ApplicationEvent {
    
    private final Long userId;  // 수신자 ID
    private final NotificationCreateCommand command;  // 알림 생성 명령
    
    public NotificationCreatedEvent(Object source, Long userId, NotificationCreateCommand command) {
        super(source);
        this.userId = userId;
        this.command = command;
    }
}
