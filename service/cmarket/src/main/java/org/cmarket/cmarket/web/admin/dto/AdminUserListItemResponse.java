package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.AdminUserListItemDto;
import org.cmarket.cmarket.domain.auth.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 어드민 유저 목록 항목 응답 DTO
 */
@Getter
@Builder
public class AdminUserListItemResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
    private UserRole role;
    private LocalDateTime createdAt;
    private String status;

    public static AdminUserListItemResponse fromDto(AdminUserListItemDto dto) {
        return AdminUserListItemResponse.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .name(dto.getName())
                .nickname(dto.getNickname())
                .birthDate(dto.getBirthDate())
                .addressSido(dto.getAddressSido())
                .addressGugun(dto.getAddressGugun())
                .role(dto.getRole())
                .createdAt(dto.getCreatedAt())
                .status(dto.getStatus())
                .build();
    }
}
