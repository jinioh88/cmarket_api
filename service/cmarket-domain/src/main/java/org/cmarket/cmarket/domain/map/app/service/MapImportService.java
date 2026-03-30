package org.cmarket.cmarket.domain.map.app.service;

import org.cmarket.cmarket.domain.map.app.dto.HospitalImportCommand;
import org.cmarket.cmarket.domain.map.app.dto.HospitalImportResultDto;
import org.cmarket.cmarket.domain.map.app.dto.PetFriendlyPlaceImportCommand;

import java.util.List;

public interface MapImportService {

    HospitalImportResultDto importHospitals(List<HospitalImportCommand> commands);

    HospitalImportResultDto importPetFriendlyPlaces(List<PetFriendlyPlaceImportCommand> commands);
}
