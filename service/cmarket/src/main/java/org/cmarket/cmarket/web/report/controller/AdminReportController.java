package org.cmarket.cmarket.web.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmarket.cmarket.domain.report.app.dto.ReportDto;
import org.cmarket.cmarket.domain.report.app.dto.ReportReviewCommand;
import org.cmarket.cmarket.domain.report.app.service.ReportQueryService;
import org.cmarket.cmarket.domain.report.app.service.ReportService;
import org.cmarket.cmarket.domain.report.model.ReportStatus;
import org.cmarket.cmarket.domain.report.model.ReportTargetType;
import org.cmarket.cmarket.web.common.response.ResponseCode;
import org.cmarket.cmarket.web.common.response.SuccessResponse;
import org.cmarket.cmarket.web.report.dto.AdminReportResponse;
import org.cmarket.cmarket.web.report.dto.ReportListResponse;
import org.cmarket.cmarket.web.report.dto.ReportResponse;
import org.cmarket.cmarket.web.report.dto.ReportReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;
    private final ReportQueryService reportQueryService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<ReportListResponse>> getReports(
            @RequestParam(required = false) ReportTargetType targetType,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<AdminReportResponse> pageResult = reportQueryService.getReports(targetType, status, pageable)
                .map(AdminReportResponse::fromDto);

        ReportListResponse response = ReportListResponse.fromPage(pageResult);
        return ResponseEntity.ok(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{reportId}/review")
    public ResponseEntity<SuccessResponse<ReportResponse>> reviewReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportReviewRequest request
    ) {
        ReportReviewCommand command = request.toCommand(reportId);
        // 관리자 이메일은 필요 시 SecurityUtils로 추출 가능하지만, 현재 로직에서는 사용하지 않음
        ReportDto result = reportService.reviewReport(null, command);
        ReportResponse response = ReportResponse.fromDto(result);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<>(ResponseCode.SUCCESS, response));
    }
}

