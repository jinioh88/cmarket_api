package org.cmarket.cmarket.domain.report.app.exception;

import org.cmarket.cmarket.domain.exception.BusinessException;
import org.cmarket.cmarket.domain.exception.ErrorCode;

/**
 * 자기 자신을 차단하려고 할 때 발생하는 예외.
 */
public class BlockSelfNotAllowedException extends BusinessException {

    public BlockSelfNotAllowedException() {
        super(ErrorCode.BLOCK_SELF_NOT_ALLOWED);
    }

    public BlockSelfNotAllowedException(String message) {
        super(ErrorCode.BLOCK_SELF_NOT_ALLOWED, message);
    }
}

