package org.cmarket.cmarket.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 사용자 정보 웹 DTO
 * 
 * 웹 계층에서 사용하는 사용자 정보 DTO입니다.
 */
@Getter
@Builder
public class UserWebDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
    
    /**
     * 앱 DTO에서 웹 DTO로 변환
     * 
     * @param userDto 앱 DTO
     * @return 웹 DTO
     */
    public static UserWebDto fromDto(org.cmarket.cmarket.domain.auth.app.dto.UserDto userDto) {
        return UserWebDto.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .nickname(userDto.getNickname())
                .birthDate(userDto.getBirthDate())
                .addressSido(userDto.getAddressSido())
                .addressGugun(userDto.getAddressGugun())
                .build();
    }
}

