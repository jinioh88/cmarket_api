package org.cmarket.cmarket.web.eventfeed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.eventfeed.app.dto.EventIngestCommand;
import org.cmarket.cmarket.domain.eventfeed.model.EventIngestStatus;

import java.time.LocalDate;

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
                .build();
    }
}
