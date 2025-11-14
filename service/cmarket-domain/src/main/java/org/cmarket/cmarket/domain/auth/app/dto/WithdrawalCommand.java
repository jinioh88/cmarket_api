package org.cmarket.cmarket.domain.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.model.WithdrawalReasonType;

/**
 * 회원 탈퇴 명령 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class WithdrawalCommand {
    private String email;  // 사용자 이메일 (컨트롤러에서 Authentication에서 추출)
    private WithdrawalReasonType reason;
    private String detailReason;
}

