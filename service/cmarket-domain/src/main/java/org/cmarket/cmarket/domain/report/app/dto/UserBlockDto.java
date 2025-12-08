package org.cmarket.cmarket.domain.report.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 차단 결과 DTO.
 */
@Getter
@Builder
public class UserBlockDto {
    private Long blockerId;
    private Long blockedUserId;
    private String blockedNickname;
    private String blockedProfileImageUrl;
    private LocalDateTime createdAt;
}

