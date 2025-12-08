package org.cmarket.cmarket.web.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신고 응답 DTO
 * 
 * 웹 계층에서 사용하는 신고 응답 DTO입니다.
 */
@Getter
@Builder
public class ReportResponse {
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
     * 앱 DTO에서 웹 DTO로 변환
     * 
     * @param reportDto 앱 DTO
     * @return ReportResponse
     */
    public static ReportResponse fromDto(ReportDto reportDto) {
        return ReportResponse.builder()
                .id(reportDto.getId())
                .reporterId(reportDto.getReporterId())
                .targetType(reportDto.getTargetType())
                .targetId(reportDto.getTargetId())
                .reasonCodes(reportDto.getReasonCodes())
                .detailReason(reportDto.getDetailReason())
                .imageUrls(reportDto.getImageUrls())
                .status(reportDto.getStatus())
                .createdAt(reportDto.getCreatedAt())
                .build();
    }
}

