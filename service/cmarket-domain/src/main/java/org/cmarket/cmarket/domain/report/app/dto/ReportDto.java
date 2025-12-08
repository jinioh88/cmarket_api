package org.cmarket.cmarket.domain.report.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.model.Report;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신고 정보 DTO
 * 
 * 앱 서비스에서 사용하는 DTO입니다.
 */
@Getter
@Builder
public class ReportDto {
    private Long id;
    private Long reporterId;
    private ReportTargetType targetType;
    private Long targetId;
    private List<String> reasonCodes;
    private String detailReason;
    private List<String> imageUrls;
    private ReportStatus status;
    private LocalDateTime createdAt;
    
    /**
     * Report 엔티티에서 DTO로 변환
     * 
     * @param report Report 엔티티
     * @return ReportDto
     */
    public static ReportDto fromEntity(Report report) {
        return ReportDto.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reasonCodes(report.getReasonCodes())
                .detailReason(report.getDetailReason())
                .imageUrls(report.getImageUrls())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}

