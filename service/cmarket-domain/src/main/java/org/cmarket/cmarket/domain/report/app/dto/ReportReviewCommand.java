package org.cmarket.cmarket.domain.report.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.model.ReportStatus;

@Getter
@Builder
public class ReportReviewCommand {
    private Long reportId;
    private ReportStatus status;
    private String rejectedReason;
    private String actionNote;
}

