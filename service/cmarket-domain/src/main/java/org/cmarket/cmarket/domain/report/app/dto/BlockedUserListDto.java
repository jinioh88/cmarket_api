package org.cmarket.cmarket.domain.report.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

/**
 * 차단한 유저 목록 DTO
 * 
 * 앱 서비스에서 사용하는 차단한 유저 목록 DTO입니다.
 */
@Getter
@Builder
public class BlockedUserListDto {
    private PageResult<BlockedUserDto> blockedUsers;
}

