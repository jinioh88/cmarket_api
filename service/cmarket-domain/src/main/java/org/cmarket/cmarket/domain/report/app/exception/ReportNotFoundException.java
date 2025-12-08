package org.cmarket.cmarket.domain.report.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

public class ReportNotFoundException extends BusinessException {

    public ReportNotFoundException() {
        super(ErrorCode.REPORT_NOT_FOUND);
    }

    public ReportNotFoundException(String message) {
        super(ErrorCode.REPORT_NOT_FOUND, message);
    }
}

