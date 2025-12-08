package org.cmarket.cmarket.domain.report.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 신고 엔티티.
 */
@Entity
@Table(
    name = "reports",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_reporter_target",
            columnNames = {"reporter_id", "target_type", "target_id"}
        )
    },
    indexes = {
        @Index(name = "idx_report_target_type", columnList = "target_type"),
        @Index(name = "idx_report_status", columnList = "status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "reporter_id")
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "target_type", length = 30)
    private ReportTargetType targetType;

    @Column(nullable = false, name = "target_id")
    private Long targetId;

    @ElementCollection
    @CollectionTable(
        name = "report_reasons",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @Column(name = "reason_code", length = 100)
    private List<String> reasonCodes = new ArrayList<>();

    @Column(name = "detail_reason", length = 300)
    private String detailReason;

    @ElementCollection
    @CollectionTable(
        name = "report_images",
        joinColumns = @JoinColumn(name = "report_id")
    )
    @Column(name = "image_url", length = 500)
    private List<String> imageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejected_reason", length = 300)
    private String rejectedReason;

    @Column(name = "action_note", length = 500)
    private String actionNote;

    @Builder
    public Report(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId,
            List<String> reasonCodes,
            String detailReason,
            List<String> imageUrls
    ) {
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reasonCodes = reasonCodes != null ? new ArrayList<>(reasonCodes) : new ArrayList<>();
        this.detailReason = detailReason;
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
        this.status = ReportStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void review(ReportStatus newStatus, String rejectedReason) {
        this.status = newStatus;
        this.rejectedReason = rejectedReason;
        this.reviewedAt = LocalDateTime.now();
    }
}

