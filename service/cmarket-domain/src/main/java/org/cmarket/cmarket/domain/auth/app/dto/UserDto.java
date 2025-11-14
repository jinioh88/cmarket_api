package org.cmarket.cmarket.domain.auth.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 사용자 정보 DTO
 * 
 * 앱 서비스에서 사용하는 사용자 정보 DTO입니다.
 */
@Getter
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
}

