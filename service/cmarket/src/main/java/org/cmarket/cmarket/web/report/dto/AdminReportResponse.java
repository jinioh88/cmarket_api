package org.cmarket.cmarket.web.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterNickname;  // 신고자 닉네임
    private ReportTargetType targetType;
    private Long targetId;
    private String targetNickname;   // 신고 대상자 닉네임
    private BoardType boardType;     // COMMUNITY_POST일 때만 값 있음 (nullable)
    private List<String> reasonCodes;
    private String detailReason;
    private List<String> imageUrls;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String rejectedReason;

    public static AdminReportResponse fromDto(ReportDto dto) {
        return AdminReportResponse.builder()
                .id(dto.getId())
                .reporterId(dto.getReporterId())
                .reporterNickname(dto.getReporterNickname())
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .targetNickname(dto.getTargetNickname())
                .boardType(dto.getBoardType())
                .reasonCodes(dto.getReasonCodes())
                .detailReason(dto.getDetailReason())
                .imageUrls(dto.getImageUrls())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}

