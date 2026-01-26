package org.cmarket.cmarket.domain.profile.app.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * 프로필 수정 명령 DTO
 * 
 * 앱 서비스에서 사용하는 프로필 수정 명령 DTO입니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 추가 (JPA/Jackson용)
@AllArgsConstructor
public class ProfileUpdateCommand {
    private String email;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
    private String profileImageUrl;
    private String introduction;
}

