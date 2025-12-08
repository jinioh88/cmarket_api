package org.cmarket.cmarket.web.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class ReportListResponse {
    private List<AdminReportResponse> reports;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static ReportListResponse fromPage(Page<AdminReportResponse> pageData) {
        return ReportListResponse.builder()
                .reports(pageData.getContent())
                .page(pageData.getNumber())
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
    }
}

