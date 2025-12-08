package org.cmarket.cmarket.domain.report.app.service;

import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 신고 조회 서비스.
 */
public interface ReportQueryService {

    /**
     * 신고 목록 조회 (필터: targetType, status).
     *
     * @param targetType 신고 대상 타입 (nullable)
     * @param status 신고 상태 (nullable)
     * @param pageable 페이지네이션 정보
     * @return 신고 목록 페이지
     */
    Page<ReportDto> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable);
}

