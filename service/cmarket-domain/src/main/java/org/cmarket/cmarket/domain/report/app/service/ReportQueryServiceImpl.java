package org.cmarket.cmarket.domain.report.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.repository.PostRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryServiceImpl implements ReportQueryService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;

    @Override
    public Page<ReportDto> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable) {
        // 우선 순위: targetType + status 조합은 레포지토리 메서드 사용
        if (targetType != null && status != null) {
            Page<Report> page = reportRepository.findByTargetTypeAndStatusOrderByCreatedAtDesc(targetType, status, pageable);
            return page.map(report -> ReportDto.fromEntity(report, buildPostBoardTypeMap(page.getContent())));
        }

        // 나머지 조합은 전체 조회 후 필터링 (정렬: createdAt DESC)
        List<Report> all = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Report> filtered = all.stream()
                .filter(report -> targetType == null || report.getTargetType() == targetType)
                .filter(report -> status == null || report.getStatus() == status)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Report> pageContent = filtered.subList(Math.min(start, filtered.size()), end);
        Map<Long, BoardType> postBoardTypes = buildPostBoardTypeMap(pageContent);
        List<ReportDto> content = pageContent.stream()
                .map(report -> ReportDto.fromEntity(report, postBoardTypes))
                .toList();

        return new PageImpl<>(content, pageable, filtered.size());
    }

    /**
     * COMMUNITY_POST 신고의 targetId에 해당하는 Post를 한 번에 조회하여 boardType 매핑 생성 (N+1 방지)
     */
    private Map<Long, BoardType> buildPostBoardTypeMap(List<Report> reports) {
        List<Long> postIds = reports.stream()
                .filter(r -> r.getTargetType() == ReportTargetType.COMMUNITY_POST)
                .map(Report::getTargetId)
                .distinct()
                .toList();

        if (postIds.isEmpty()) {
            return Map.of();
        }

        return postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, Post::getBoardType));
    }
}

