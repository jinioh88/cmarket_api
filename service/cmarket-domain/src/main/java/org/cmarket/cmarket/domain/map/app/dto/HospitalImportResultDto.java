package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HospitalImportResultDto {

    private Integer requestedCount;
    private Integer importedCount;
    private Integer skippedCount;
}
