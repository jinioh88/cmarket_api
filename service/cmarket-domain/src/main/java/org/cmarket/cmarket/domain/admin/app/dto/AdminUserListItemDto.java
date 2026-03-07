package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 어드민 유저 목록 항목 DTO
 */
@Getter
@Builder
public class AdminUserListItemDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
    private UserRole role;
    private LocalDateTime createdAt;
    private String status;  // ACTIVE, WITHDRAWN
}
