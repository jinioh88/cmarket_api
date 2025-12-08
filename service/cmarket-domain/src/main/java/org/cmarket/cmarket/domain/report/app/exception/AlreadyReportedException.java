package org.cmarket.cmarket.domain.report.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 이미 신고된 대상에 대해 중복 신고 시도 시 발생하는 예외
 */
public class AlreadyReportedException extends BusinessException {
    
    public AlreadyReportedException() {
        super(ErrorCode.ALREADY_REPORTED);
    }
    
    public AlreadyReportedException(String message) {
        super(ErrorCode.ALREADY_REPORTED, message);
    }
}

