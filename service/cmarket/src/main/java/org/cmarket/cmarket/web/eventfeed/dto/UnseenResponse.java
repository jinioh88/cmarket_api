package org.cmarket.cmarket.web.eventfeed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 처음 보는 출처 글 번호 응답 DTO
 */
@Getter
@AllArgsConstructor
public class UnseenResponse {
    private final List<String> unseen;
}
