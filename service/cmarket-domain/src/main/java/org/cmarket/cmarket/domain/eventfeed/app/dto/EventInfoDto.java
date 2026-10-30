package org.cmarket.cmarket.domain.eventfeed.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 행사 글에 붙는 행사 칸 DTO
 * 
 * 커뮤니티 목록과 상세가 기간, 지역, 주최, 한 줄 소개를 본문 글자에서 꺼내지 않고 이 칸으로 보인다.
 */
@Getter
@AllArgsConstructor
public class EventInfoDto {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String region;
    private final String place;
    private final String organizer;
    private final String summary;
    private final String target;
    private final String fee;
    private final List<String> activities;
    private final List<String> checks;
    private final String sourceUrl;
    private final String imageUrl;

    public static EventInfoDto from(EventIngestRecord record) {
        return new EventInfoDto(
                record.getEventStartDate(),
                record.getEventEndDate(),
                record.getRegion(),
                record.getPlace(),
                record.getOrganizer(),
                record.getSummary(),
                record.getTarget(),
                record.getFee(),
                lines(record.getActivities()),
                lines(record.getChecks()),
                record.getSourceUrl(),
                record.getImageUrl()
        );
    }

    private static List<String> lines(String joined) {
        return joined == null || joined.isBlank() ? List.of() : List.of(joined.split("\n"));
    }
}
