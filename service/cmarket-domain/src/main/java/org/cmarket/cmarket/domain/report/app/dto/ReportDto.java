package org.cmarket.cmarket.domain.report.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.report.model.Report;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 신고 정보 DTO
 *
 * 앱 서비스에서 사용하는 DTO입니다.
 * boardType은 targetType이 COMMUNITY_POST일 때만 값이 있으며, Post 조회를 통해 보강됩니다.
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
    private BoardType boardType;  // COMMUNITY_POST일 때만 값 있음 (nullable)

    /**
     * Report 엔티티에서 DTO로 변환
     *
     * @param report Report 엔티티
     * @return ReportDto
     */
    public static ReportDto fromEntity(Report report) {
        return fromEntity(report, null);
    }

    /**
     * Report 엔티티에서 DTO로 변환 (boardType 보강)
     * targetType이 COMMUNITY_POST일 때 postBoardTypes에서 boardType을 조회하여 설정합니다.
     *
     * @param report Report 엔티티
     * @param postBoardTypes targetId -> BoardType 매핑 (COMMUNITY_POST 신고용)
     * @return ReportDto
     */
    public static ReportDto fromEntity(Report report, Map<Long, BoardType> postBoardTypes) {
        BoardType boardType = null;
        if (report.getTargetType() == ReportTargetType.COMMUNITY_POST && postBoardTypes != null) {
            boardType = postBoardTypes.get(report.getTargetId());
        }

        return ReportDto.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reasonCodes(report.getReasonCodes() != null ? new ArrayList<>(report.getReasonCodes()) : new ArrayList<>())
                .detailReason(report.getDetailReason())
                .imageUrls(report.getImageUrls() != null ? new ArrayList<>(report.getImageUrls()) : new ArrayList<>())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .boardType(boardType)
                .build();
    }
}

