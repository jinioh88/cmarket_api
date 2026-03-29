package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HospitalImportRangeRequest {

    private Integer startPage = 1;
    private Integer endPage = 10;
    private Integer numOfRows = 100;
}
