package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.domain.map.model.HospitalDetail;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class HospitalDetailInfoDto {

    private Boolean is24Hours;
    private Boolean isEmergencyAvailable;
    private List<AnimalType> animalTypes;

    public static HospitalDetailInfoDto fromEntity(HospitalDetail hospitalDetail) {
        if (hospitalDetail == null) {
            return null;
        }

        return HospitalDetailInfoDto.builder()
                .is24Hours(hospitalDetail.getIs24Hours())
                .isEmergencyAvailable(hospitalDetail.getIsEmergencyAvailable())
                .animalTypes(new ArrayList<>(hospitalDetail.getAnimalTypes()))
                .build();
    }
}
