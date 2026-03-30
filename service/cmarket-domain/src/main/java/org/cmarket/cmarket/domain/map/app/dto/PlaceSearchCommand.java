package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

import java.util.List;

@Getter
@Builder
public class PlaceSearchCommand {

    private PlaceCategory category;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;
    private Boolean isRecommended;
    private Boolean is24Hours;
    private Boolean isEmergencyAvailable;
    private List<AnimalType> animalTypes;
    private Integer page;
    private Integer size;
}
