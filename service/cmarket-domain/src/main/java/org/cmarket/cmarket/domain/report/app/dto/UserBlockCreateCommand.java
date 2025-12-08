package org.cmarket.cmarket.domain.report.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 차단 생성 명령 DTO.
 */
@Getter
@Builder
public class UserBlockCreateCommand {
    private Long blockedUserId;
}

