package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.AdminUserDto;
import org.cmarket.cmarket.domain.auth.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 어드민용 회원 정보 응답 DTO
 */
@Getter
@Builder
public class AdminUserResponse {

    private Long id;
    private String email;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
    private UserRole role;
    private LocalDateTime createdAt;

    public static AdminUserResponse fromDto(AdminUserDto dto) {
        return AdminUserResponse.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .name(dto.getName())
                .nickname(dto.getNickname())
                .birthDate(dto.getBirthDate())
                .addressSido(dto.getAddressSido())
                .addressGugun(dto.getAddressGugun())
                .role(dto.getRole())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
