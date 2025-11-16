package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 차단한 유저 정보 웹 DTO
 * 
 * 웹 계층에서 사용하는 차단한 유저 정보 DTO입니다.
 */
@Getter
@Builder
public class BlockedUserResponse {
    private Long blockedUserId;
    private String nickname;
    private String profileImageUrl;
}

