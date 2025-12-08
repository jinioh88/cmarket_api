package org.cmarket.cmarket.web.report.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.app.dto.ReportReviewCommand;
import org.cmarket.cmarket.domain.report.model.ReportStatus;

@Getter
public class ReportReviewRequest {

    @NotNull(message = "신고 상태는 필수입니다.")
    private ReportStatus status;

    @Size(max = 300, message = "거절 사유는 최대 300자까지 입력 가능합니다.")
    private String rejectedReason;

    @Size(max = 500, message = "조치 메모는 최대 500자까지 입력 가능합니다.")
    private String actionNote;

    public ReportReviewCommand toCommand(Long reportId) {
        return ReportReviewCommand.builder()
                .reportId(reportId)
                .status(status)
                .rejectedReason(rejectedReason)
                .actionNote(actionNote)
                .build();
    }
}

