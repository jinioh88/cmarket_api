package org.cmarket.cmarket.domain.report.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

public class ReportStatusInvalidException extends BusinessException {

    public ReportStatusInvalidException() {
        super(ErrorCode.REPORT_STATUS_INVALID);
    }

    public ReportStatusInvalidException(String message) {
        super(ErrorCode.REPORT_STATUS_INVALID, message);
    }
}

