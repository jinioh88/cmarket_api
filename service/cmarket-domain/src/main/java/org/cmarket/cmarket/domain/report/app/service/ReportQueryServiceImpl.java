package org.cmarket.cmarket.domain.report.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.model.Report;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.cmarket.cmarket.domain.report.repository.ReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryServiceImpl implements ReportQueryService {

    private final ReportRepository reportRepository;

    @Override
    public Page<ReportDto> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable) {
        // 우선 순위: targetType + status 조합은 레포지토리 메서드 사용
        if (targetType != null && status != null) {
            Page<Report> page = reportRepository.findByTargetTypeAndStatusOrderByCreatedAtDesc(targetType, status, pageable);
            return page.map(ReportDto::fromEntity);
        }

        // 나머지 조합은 전체 조회 후 필터링 (정렬: createdAt DESC)
        List<Report> all = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Report> filtered = all.stream()
                .filter(report -> targetType == null || report.getTargetType() == targetType)
                .filter(report -> status == null || report.getStatus() == status)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ReportDto> content = filtered.subList(Math.min(start, filtered.size()), end)
                .stream()
                .map(ReportDto::fromEntity)
                .toList();

        return new PageImpl<>(content, pageable, filtered.size());
    }
}

