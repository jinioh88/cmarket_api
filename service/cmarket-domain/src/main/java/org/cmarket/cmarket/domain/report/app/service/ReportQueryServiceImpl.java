package org.cmarket.cmarket.domain.report.app.service;

import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.auth.model.User;
import org.cmarket.cmarket.domain.auth.repository.UserRepository;
import org.cmarket.cmarket.domain.community.model.BoardType;
import org.cmarket.cmarket.domain.community.model.Post;
import org.cmarket.cmarket.domain.community.repository.PostRepository;
import org.cmarket.cmarket.domain.product.model.Product;
import org.cmarket.cmarket.domain.product.repository.ProductRepository;
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
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public Page<ReportDto> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable) {
        // 우선 순위: targetType + status 조합은 레포지토리 메서드 사용
        if (targetType != null && status != null) {
            Page<Report> page = reportRepository.findByTargetTypeAndStatusOrderByCreatedAtDesc(targetType, status, pageable);
            ReportDto.ReportEnrichment enrichment = buildEnrichment(page.getContent());
            return page.map(report -> ReportDto.fromEntity(report, enrichment));
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
        ReportDto.ReportEnrichment enrichment = buildEnrichment(pageContent);
        List<ReportDto> content = pageContent.stream()
                .map(report -> ReportDto.fromEntity(report, enrichment))
                .toList();

        return new PageImpl<>(content, pageable, filtered.size());
    }

    /**
     * 신고 목록 조회 시 닉네임, boardType 등 보강 데이터 생성 (N+1 방지)
     */
    private ReportDto.ReportEnrichment buildEnrichment(List<Report> reports) {
        // 1. reporterIds, USER targetIds 수집
        List<Long> reporterIds = reports.stream().map(Report::getReporterId).distinct().toList();
        List<Long> userTargetIds = reports.stream()
                .filter(r -> r.getTargetType() == ReportTargetType.USER)
                .map(Report::getTargetId)
                .distinct()
                .toList();

        // 2. PRODUCT targetIds -> sellerIds
        List<Long> productTargetIds = reports.stream()
                .filter(r -> r.getTargetType() == ReportTargetType.PRODUCT)
                .map(Report::getTargetId)
                .distinct()
                .toList();
        Map<Long, Long> productIdToSellerId = Map.of();
        List<Long> sellerIds = List.of();
        if (!productTargetIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(productTargetIds);
            productIdToSellerId = products.stream()
                    .collect(Collectors.toMap(Product::getId, Product::getSellerId));
            sellerIds = products.stream().map(Product::getSellerId).distinct().toList();
        }

        // 3. COMMUNITY_POST targetIds
        List<Long> postIds = reports.stream()
                .filter(r -> r.getTargetType() == ReportTargetType.COMMUNITY_POST)
                .map(Report::getTargetId)
                .distinct()
                .toList();

        // 4. User 일괄 조회 (reporter + USER target + PRODUCT seller)
        List<Long> allUserIds = java.util.stream.Stream.concat(
                java.util.stream.Stream.concat(reporterIds.stream(), userTargetIds.stream()),
                sellerIds.stream()
        ).distinct().toList();
        Map<Long, String> userIdToNickname = Map.of();
        if (!allUserIds.isEmpty()) {
            userIdToNickname = userRepository.findAllById(allUserIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
        }

        // 5. Post 일괄 조회 (boardType + authorNickname)
        Map<Long, BoardType> postBoardTypes = Map.of();
        Map<Long, String> postIdToAuthorNickname = Map.of();
        if (!postIds.isEmpty()) {
            List<Post> posts = postRepository.findAllById(postIds);
            postBoardTypes = posts.stream().collect(Collectors.toMap(Post::getId, Post::getBoardType));
            postIdToAuthorNickname = posts.stream()
                    .collect(Collectors.toMap(Post::getId, p -> p.getAuthorNickname() != null ? p.getAuthorNickname() : ""));
        }

        return new ReportDto.ReportEnrichment(postBoardTypes, userIdToNickname, productIdToSellerId, postIdToAuthorNickname);
    }
}

