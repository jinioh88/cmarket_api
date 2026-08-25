package org.cmarket.cmarket.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 계정 찾기(가입 방법 안내) 요청 DTO (#849)
 *
 * ⚠️ 응답 DTO 는 만들지 않는다. 돌려줄 알맹이가 없고 문구만 주기 때문에
 *    SuccessResponse<String> 을 그대로 쓴다 — /password/reset/send 와 같은 결이다.
 */
@Getter
@NoArgsConstructor
public class AccountFindRequest {
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
