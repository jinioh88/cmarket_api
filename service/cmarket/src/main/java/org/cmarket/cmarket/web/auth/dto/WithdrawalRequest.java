package org.cmarket.cmarket.web.auth.dto;

import org.cmarket.cmarket.domain.auth.model.WithdrawalReasonType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 요청 DTO
 */
@Getter
@NoArgsConstructor
public class WithdrawalRequest {
    
    @NotNull(message = "탈퇴 사유는 필수입니다.")
    private WithdrawalReasonType reason;
    
    @Size(min = 2, max = 500, message = "탈퇴 상세 사유는 2자 이상 500자 이하여야 합니다.")
    private String detailReason;  // 선택 사항
}

