package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CafeImportRequest {

    private Integer startPage = 1;
    private Integer endPage = 10;
    private Integer numOfRows = 100;
    private String roadNmAddrKeyword;
    private String businessNameKeyword = "카페";
    private String salesStatusCode = "01";
}
