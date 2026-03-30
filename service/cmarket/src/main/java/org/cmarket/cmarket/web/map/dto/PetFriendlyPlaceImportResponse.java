package org.cmarket.cmarket.web.map.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.app.dto.HospitalImportResultDto;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@Builder
public class PetFriendlyPlaceImportResponse {

    private PlaceCategory category;
    private Integer fetchedCount;
    private Integer requestedStartPage;
    private Integer requestedEndPage;
    private Integer requestedNumOfRows;
    private Integer processedPages;
    private Integer apiTotalCount;
    private Integer importedCount;
    private Integer skippedCount;

    public static PetFriendlyPlaceImportResponse of(
            PlaceCategory category,
            int fetchedCount,
            int requestedStartPage,
            int requestedEndPage,
            int requestedNumOfRows,
            int apiTotalCount,
            HospitalImportResultDto importResult
    ) {
        return PetFriendlyPlaceImportResponse.builder()
                .category(category)
                .fetchedCount(fetchedCount)
                .requestedStartPage(requestedStartPage)
                .requestedEndPage(requestedEndPage)
                .requestedNumOfRows(requestedNumOfRows)
                .processedPages(requestedEndPage - requestedStartPage + 1)
                .apiTotalCount(apiTotalCount)
                .importedCount(importResult.getImportedCount())
                .skippedCount(importResult.getSkippedCount())
                .build();
    }
}
