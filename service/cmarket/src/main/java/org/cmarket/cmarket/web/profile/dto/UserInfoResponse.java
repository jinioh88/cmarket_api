package org.cmarket.cmarket.web.profile.dto;

/**
 * 사용자 정보 응답 DTO
 * 
 * @deprecated UserProfileResponse를 직접 사용하세요.
 *             본인 정보 조회 시에는 UserProfileResponse.fromMyPageDto()를 사용하세요.
 */
@Deprecated
public class UserInfoResponse {
    /**
     * 앱 DTO에서 웹 DTO로 변환
     * 
     * @param myPageDto 앱 DTO
     * @return 웹 DTO
     */
    public static UserProfileResponse fromDto(org.cmarket.cmarket.domain.profile.app.dto.MyPageDto myPageDto) {
        return UserProfileResponse.fromMyPageDto(myPageDto);
    }
}

