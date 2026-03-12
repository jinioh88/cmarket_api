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
    private String reporterNickname;  // 신고자 닉네임
    private ReportTargetType targetType;
    private Long targetId;
    private String targetNickname;    // 신고 대상자 닉네임 (USER: 피신고자, PRODUCT: 판매자, COMMUNITY_POST: 게시글 작성자)
    private String title;             // PRODUCT: 상품명, COMMUNITY_POST: 제목, USER: null
    private List<String> reasonCodes;
    private String detailReason;
    private List<String> imageUrls;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String rejectedReason;
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
     * Report 엔티티에서 DTO로 변환 (boardType, 닉네임 보강)
     *
     * @param report Report 엔티티
     * @param enrichment 보강 데이터 (boardType, reporterNickname, targetNickname)
     * @return ReportDto
     */
    public static ReportDto fromEntity(Report report, ReportEnrichment enrichment) {
        BoardType boardType = null;
        String reporterNickname = null;
        String targetNickname = null;
        String title = null;

        if (enrichment != null) {
            if (report.getTargetType() == ReportTargetType.COMMUNITY_POST && enrichment.postBoardTypes() != null) {
                boardType = enrichment.postBoardTypes().get(report.getTargetId());
            }
            if (enrichment.userIdToNickname() != null) {
                reporterNickname = enrichment.userIdToNickname().get(report.getReporterId());
            }
            targetNickname = enrichment.getTargetNickname(report.getTargetType(), report.getTargetId());
            title = enrichment.getTitle(report.getTargetType(), report.getTargetId());
        }

        return ReportDto.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporterNickname(reporterNickname)
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .targetNickname(targetNickname)
                .title(title)
                .reasonCodes(report.getReasonCodes() != null ? new ArrayList<>(report.getReasonCodes()) : new ArrayList<>())
                .detailReason(report.getDetailReason())
                .imageUrls(report.getImageUrls() != null ? new ArrayList<>(report.getImageUrls()) : new ArrayList<>())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .reviewedAt(report.getReviewedAt())
                .rejectedReason(report.getRejectedReason())
                .boardType(boardType)
                .build();
    }

    /**
     * 신고 목록 조회 시 사용하는 보강 데이터
     */
    public record ReportEnrichment(
            Map<Long, BoardType> postBoardTypes,
            Map<Long, String> userIdToNickname,
            Map<Long, Long> productIdToSellerId,
            Map<Long, String> postIdToAuthorNickname,
            Map<Long, String> productIdToTitle,
            Map<Long, String> postIdToTitle
    ) {
        public String getTargetNickname(ReportTargetType targetType, Long targetId) {
            if (targetType == null || targetId == null) return null;
            return switch (targetType) {
                case USER -> userIdToNickname != null ? userIdToNickname.get(targetId) : null;
                case PRODUCT -> {
                    if (productIdToSellerId == null || userIdToNickname == null) yield null;
                    Long sellerId = productIdToSellerId.get(targetId);
                    yield sellerId != null ? userIdToNickname.get(sellerId) : null;
                }
                case COMMUNITY_POST -> postIdToAuthorNickname != null ? postIdToAuthorNickname.get(targetId) : null;
            };
        }

        public String getTitle(ReportTargetType targetType, Long targetId) {
            if (targetType == null || targetId == null) return null;
            return switch (targetType) {
                case USER -> null;
                case PRODUCT -> productIdToTitle != null ? productIdToTitle.get(targetId) : null;
                case COMMUNITY_POST -> postIdToTitle != null ? postIdToTitle.get(targetId) : null;
            };
        }
    }
}

