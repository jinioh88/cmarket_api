package org.cmarket.cmarket.web.map.dto;

import org.cmarket.cmarket.domain.map.app.dto.HospitalImportCommand;

import java.util.List;

public record PublicAnimalHospitalFetchResult(
        int totalCount,
        List<HospitalImportCommand> hospitals
) {
}
