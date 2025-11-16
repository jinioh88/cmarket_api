package org.cmarket.cmarket.domain.profile.app.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 차단한 유저 정보 DTO
 * 
 * 앱 서비스에서 사용하는 차단한 유저 정보 DTO입니다.
 */
@Getter
@Builder
public class BlockedUserDto {
    private Long blockedUserId;
    private String nickname;
    private String profileImageUrl;
}

