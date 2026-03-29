package org.cmarket.cmarket.web.map.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.app.dto.HospitalImportResultDto;

@Getter
@Builder
public class HospitalImportResponse {

    private Integer fetchedCount;
    private Integer requestedPageNo;
    private Integer requestedNumOfRows;
    private Boolean importAllPages;
    private Integer requestedStartPage;
    private Integer requestedEndPage;
    private Integer processedPages;
    private Integer apiTotalCount;
    private Integer importedCount;
    private Integer skippedCount;

    public static HospitalImportResponse of(
            int fetchedCount,
            int requestedPageNo,
            int requestedNumOfRows,
            boolean importAllPages,
            int apiTotalCount,
            HospitalImportResultDto importResult
    ) {
        return HospitalImportResponse.builder()
                .fetchedCount(fetchedCount)
                .requestedPageNo(requestedPageNo)
                .requestedNumOfRows(requestedNumOfRows)
                .importAllPages(importAllPages)
                .requestedStartPage(requestedPageNo)
                .requestedEndPage(requestedPageNo)
                .processedPages(1)
                .apiTotalCount(apiTotalCount)
                .importedCount(importResult.getImportedCount())
                .skippedCount(importResult.getSkippedCount())
                .build();
    }

    public static HospitalImportResponse ofPageRange(
            int fetchedCount,
            int requestedStartPage,
            int requestedEndPage,
            int requestedNumOfRows,
            int apiTotalCount,
            HospitalImportResultDto importResult
    ) {
        return HospitalImportResponse.builder()
                .fetchedCount(fetchedCount)
                .requestedPageNo(requestedStartPage)
                .requestedNumOfRows(requestedNumOfRows)
                .importAllPages(true)
                .requestedStartPage(requestedStartPage)
                .requestedEndPage(requestedEndPage)
                .processedPages(requestedEndPage - requestedStartPage + 1)
                .apiTotalCount(apiTotalCount)
                .importedCount(importResult.getImportedCount())
                .skippedCount(importResult.getSkippedCount())
                .build();
    }
}
