package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.HospitalDetailInfoDto;
import org.cmarket.cmarket.domain.map.model.AnimalType;

import java.util.List;

@Getter
@NoArgsConstructor
public class HospitalDetailInfoResponse {

    private Boolean is24Hours;
    private Boolean isEmergencyAvailable;
    private List<AnimalType> animalTypes;

    public static HospitalDetailInfoResponse fromDto(HospitalDetailInfoDto dto) {
        if (dto == null) {
            return null;
        }

        HospitalDetailInfoResponse response = new HospitalDetailInfoResponse();
        response.is24Hours = dto.getIs24Hours();
        response.isEmergencyAvailable = dto.getIsEmergencyAvailable();
        response.animalTypes = dto.getAnimalTypes();
        return response;
    }
}
