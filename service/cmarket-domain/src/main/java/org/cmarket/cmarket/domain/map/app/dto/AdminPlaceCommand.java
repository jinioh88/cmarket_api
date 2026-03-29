package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

import java.util.List;

@Getter
@Builder
public class AdminPlaceCommand {

    private PlaceCategory category;
    private String name;
    private String address;
    private String phone;
    private String operatingHours;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private Boolean isRecommended;
    private Boolean is24Hours;
    private Boolean isEmergencyAvailable;
    private List<AnimalType> animalTypes;
}
