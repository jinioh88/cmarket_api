package org.cmarket.cmarket.web.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.app.dto.UserBlockDto;

import java.time.LocalDateTime;

/**
 * 사용자 차단 응답 DTO.
 */
@Getter
@Builder
public class UserBlockResponse {
    private Long blockedUserId;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime createdAt;

    public static UserBlockResponse fromDto(UserBlockDto dto) {
        return UserBlockResponse.builder()
                .blockedUserId(dto.getBlockedUserId())
                .nickname(dto.getBlockedNickname())
                .profileImageUrl(dto.getBlockedProfileImageUrl())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}

