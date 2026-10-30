package org.cmarket.cmarket.domain.eventfeed.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;

import java.time.LocalDate;

/**
 * 행사 수집 명령 DTO
 * 
 * status가 POSTED면 글을 만들고, 건너뜀이면 기록만 남긴다.
 */
@Getter
@Builder
public class EventIngestCommand {
    private String sourceCode;
    private String externalId;
    private String sourceUrl;
    private EventIngestStatus status;
    private String title;
    private String content;
    private String imageUrl;          // 원래 주소 그대로 (복사하지 않는다)
    private LocalDate eventStartDate;
}
