package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HospitalImportRequest {

    private Integer pageNo = 1;
    private Integer numOfRows = 100;
    private Boolean importAllPages = false;
    private String opnAtmyGrpCd;
    private String salesStatusCode;
    private String roadNmAddrKeyword;
    private String businessNameKeyword;
    private String updatedFrom;
    private String updatedTo;
}
