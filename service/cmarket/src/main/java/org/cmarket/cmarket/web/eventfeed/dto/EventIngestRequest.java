package org.cmarket.cmarket.web.eventfeed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestCommand;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * 행사 수집 요청 DTO
 * 
 * status가 POSTED면 title, content로 글을 만들고, SKIPPED_* 면 기록만 남긴다.
 */
@Getter
@NoArgsConstructor
public class EventIngestRequest {

    @NotBlank
    @Size(max = 30)
    private String sourceCode;

    @NotBlank
    @Size(max = 100)
    private String externalId;

    @NotBlank
    @Size(max = 500)
    private String sourceUrl;

    @NotBlank
    private String status;

    private String title;

    private String content;

    @Size(max = 500)
    private String imageUrl;

    private LocalDate eventStartDate;

    private LocalDate eventEndDate;

    @Size(max = 20)
    private String region;

    @Size(max = 100)
    private String place;

    @Size(max = 100)
    private String organizer;

    @Size(max = 100)
    private String summary;

    @Size(max = 100)
    private String target;

    @Size(max = 100)
    private String fee;

    @Size(max = 100)
    private String hours;

    @Size(max = 100)
    private String application;

    @Size(max = 3)
    private List<@Size(max = 200) String> activities;

    @Size(max = 2)
    private List<@Size(max = 200) String> checks;

    public EventIngestCommand toCommand() {
        return EventIngestCommand.builder()
                .sourceCode(sourceCode)
                .externalId(externalId)
                .sourceUrl(sourceUrl)
                .status(EventIngestStatus.valueOf(status))  // 모르는 값이면 IllegalArgumentException → 400
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .eventStartDate(eventStartDate)
                .eventEndDate(eventEndDate)
                .region(region)
                .place(place)
                .organizer(organizer)
                .summary(summary)
                .target(target)
                .fee(fee)
                .hours(hours)
                .application(application)
                .activities(activities)
                .checks(checks)
                .build();
    }
}
