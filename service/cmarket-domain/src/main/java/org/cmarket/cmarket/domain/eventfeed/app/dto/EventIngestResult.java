package org.cmarket.cmarket.domain.eventfeed.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 행사 수집 결과 DTO
 */
@Getter
@AllArgsConstructor
public class EventIngestResult {

    public enum Status {
        CREATED,         // 글을 만들었다
        ALREADY_EXISTS,  // 같은 (출처, 번호)가 이미 있다
        RECORDED_SKIP,   // 건너뜀으로 기록만 했다
        DUPLICATE        // 다른 출처에서 같은 행사를 이미 올렸다
    }

    private final Status status;
    private final Long postId;
}
