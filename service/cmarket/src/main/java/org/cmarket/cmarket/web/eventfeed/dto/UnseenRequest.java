package org.cmarket.cmarket.web.eventfeed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 처음 보는 출처 글 번호 조회 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UnseenRequest {

    @NotBlank
    private String sourceCode;

    @NotNull
    @Size(max = 200)
    private List<String> externalIds;
}
