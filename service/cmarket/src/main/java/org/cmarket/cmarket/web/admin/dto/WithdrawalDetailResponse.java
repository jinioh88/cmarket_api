package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.admin.app.dto.WithdrawalDetailDto;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.model.WithdrawalReasonType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 탈퇴 회원 상세 응답 DTO
 */
@Getter
@Builder
public class WithdrawalDetailResponse {
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

    public static WithdrawalDetailResponse fromDto(WithdrawalDetailDto dto) {
        return WithdrawalDetailResponse.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .name(dto.getName())
                .role(dto.getRole())
                .addressSido(dto.getAddressSido())
                .addressGugun(dto.getAddressGugun())
                .birthDate(dto.getBirthDate())
                .createdAt(dto.getCreatedAt())
                .profileImageUrl(dto.getProfileImageUrl())
                .withdrawalReason(dto.getWithdrawalReason())
                .withdrawalDetailReason(dto.getWithdrawalDetailReason())
                .deletedAt(dto.getDeletedAt())
                .build();
    }
}
