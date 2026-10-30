package org.cmarket.cmarket.web.community.dto;

import lombok.Getter;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventInfoDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 행사 글의 행사 칸 응답 DTO
 * 
 * 행사 소식(EVENT) 글일 때만 목록 항목과 상세에 붙는다. 다른 글은 null이다.
 */
@Getter
public class EventInfoResponse {
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String region;     // 목록에 보이는 넓은 지역 (서울, 제주 등)
    private final String place;      // 구체적인 장소 (상세에서 지역과 같이 보인다)
    private final String organizer;
    private final String summary;    // 한 줄 소개
    private final String target;
    private final String fee;
    private final List<String> activities;  // 이런 걸 할 수 있어요
    private final List<String> checks;      // 가기 전에 확인하세요
    private final String sourceUrl;  // 원문 주소 (「공식 안내 보기」)
    private final String imageUrl;   // 포스터 원래 주소
    private final String hours;      // 운영시간

    private EventInfoResponse(EventInfoDto dto) {
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.region = dto.getRegion();
        this.place = dto.getPlace();
        this.organizer = dto.getOrganizer();
        this.summary = dto.getSummary();
        this.target = dto.getTarget();
        this.fee = dto.getFee();
        this.activities = dto.getActivities();
        this.checks = dto.getChecks();
        this.sourceUrl = dto.getSourceUrl();
        this.imageUrl = dto.getImageUrl();
        this.hours = dto.getHours();
    }

    public static EventInfoResponse from(EventInfoDto dto) {
        return dto == null ? null : new EventInfoResponse(dto);
    }
}
