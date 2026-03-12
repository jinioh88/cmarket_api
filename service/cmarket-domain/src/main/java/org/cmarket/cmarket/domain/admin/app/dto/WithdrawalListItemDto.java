package org.cmarket.cmarket.domain.admin.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.auth.model.UserRole;
import org.cmarket.cmarket.domain.auth.model.WithdrawalReasonType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 탈퇴 회원 목록 항목 DTO
 */
@Getter
@Builder
public class WithdrawalListItemDto {
    private Long id;
    private String email;
    private String nickname;
    private String authorNickname;  // 작성자 닉네임
    private String title;           // 제목 (탈퇴 상세 사유 요약)
    private String name;
    private UserRole role;
    private String addressSido;
    private String addressGugun;
    private LocalDate birthDate;
    private WithdrawalReasonType withdrawalReason;
    private String withdrawalDetailReason;
    private LocalDateTime deletedAt;
}
