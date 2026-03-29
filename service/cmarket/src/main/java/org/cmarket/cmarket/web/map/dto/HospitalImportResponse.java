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
                .apiTotalCount(apiTotalCount)
                .importedCount(importResult.getImportedCount())
                .skippedCount(importResult.getSkippedCount())
                .build();
    }
}
