package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.model.WithdrawalReasonType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 탈퇴 회원 상세 DTO
 */
@Getter
@Builder
public class WithdrawalDetailDto {
    private Long id;
    private String email;
    private String nickname;
    private String name;
    private UserRole role;
    private String addressSido;
    private String addressGugun;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private String profileImageUrl;
    private WithdrawalReasonType withdrawalReason;
    private String withdrawalDetailReason;
    private LocalDateTime deletedAt;
}
