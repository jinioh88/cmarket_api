package org.cmarket.cmarket.web.profile.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 차단한 유저 목록 웹 DTO
 * 
 * 웹 계층에서 사용하는 차단한 유저 목록 DTO입니다.
 */
@Getter
@Builder
public class BlockedUserListResponse {
    private PageResultResponse<BlockedUserResponse> blockedUsers;
    
    /**
     * 앱 DTO에서 웹 DTO로 변환 (report 도메인)
     * 
     * @param blockedUserListDto 앱 DTO (report 도메인)
     * @return 웹 DTO
     */
    public static BlockedUserListResponse fromDto(org.cmarket.cmarket.domain.report.app.dto.BlockedUserListDto blockedUserListDto) {
        // BlockedUserDto 리스트를 BlockedUserResponse 리스트로 변환
        PageResultResponse<BlockedUserResponse> blockedUsersPageResult = 
                PageResultResponse.fromPageResult(
                        blockedUserListDto.getBlockedUsers().map(blockedUserDto -> 
                                BlockedUserResponse.builder()
                                        .blockedUserId(blockedUserDto.getBlockedUserId())
                                        .nickname(blockedUserDto.getNickname())
                                        .profileImageUrl(blockedUserDto.getProfileImageUrl())
                                        .build()
                        )
                );
        
        return BlockedUserListResponse.builder()
                .blockedUsers(blockedUsersPageResult)
                .build();
    }
    
    /**
     * 앱 DTO에서 웹 DTO로 변환 (profile 도메인)
     * 
     * @param blockedUserListDto 앱 DTO (profile 도메인)
     * @return 웹 DTO
     */
    public static BlockedUserListResponse fromDto(org.cmarket.cmarket.domain.profile.app.dto.BlockedUserListDto blockedUserListDto) {
        // BlockedUserDto 리스트를 BlockedUserResponse 리스트로 변환
        PageResultResponse<BlockedUserResponse> blockedUsersPageResult = 
                PageResultResponse.fromPageResult(
                        blockedUserListDto.getBlockedUsers().map(blockedUserDto -> 
                                BlockedUserResponse.builder()
                                        .blockedUserId(blockedUserDto.getBlockedUserId())
                                        .nickname(blockedUserDto.getNickname())
                                        .profileImageUrl(blockedUserDto.getProfileImageUrl())
                                        .build()
                        )
                );
        
        return BlockedUserListResponse.builder()
                .blockedUsers(blockedUsersPageResult)
                .build();
    }
}

