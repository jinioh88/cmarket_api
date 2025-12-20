package org.cmarket.cmarket.domain.notification.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 알림을 찾을 수 없을 때 발생하는 예외
 * 
 * 존재하지 않는 알림 ID로 조회하거나 읽음 처리할 때 발생합니다.
 */
public class NotificationNotFoundException extends BusinessException {
    
    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
    
    public NotificationNotFoundException(String message) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND, message);
    }
}
