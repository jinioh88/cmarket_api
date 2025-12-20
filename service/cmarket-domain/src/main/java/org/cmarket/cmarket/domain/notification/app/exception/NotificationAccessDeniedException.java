package org.cmarket.cmarket.domain.notification.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 알림에 대한 접근 권한이 없을 때 발생하는 예외
 * 
 * 다른 사용자의 알림을 조회하거나 읽음 처리하려고 할 때 발생합니다.
 */
public class NotificationAccessDeniedException extends BusinessException {
    
    public NotificationAccessDeniedException() {
        super(ErrorCode.NOTIFICATION_ACCESS_DENIED);
    }
    
    public NotificationAccessDeniedException(String message) {
        super(ErrorCode.NOTIFICATION_ACCESS_DENIED, message);
    }
}
