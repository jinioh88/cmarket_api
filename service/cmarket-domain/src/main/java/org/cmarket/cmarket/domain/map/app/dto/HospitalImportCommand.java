package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.AnimalType;

import java.util.List;

@Getter
@Builder
public class HospitalImportCommand {

    private String externalPlaceId;
    private String name;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private String licenseDate;
    private String salesStatusCode;
    private String salesStatusName;
    private List<AnimalType> animalTypes;
}
