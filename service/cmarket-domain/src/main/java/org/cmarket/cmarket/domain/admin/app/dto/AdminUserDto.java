package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 어드민용 사용자 정보 DTO
 *
 * 역할 변경 API 응답 등에 사용합니다.
 */
@Getter
@Builder
public class AdminUserDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private LocalDate birthDate;
    private String addressSido;
    private String addressGugun;
    private UserRole role;
    private LocalDateTime createdAt;
}
