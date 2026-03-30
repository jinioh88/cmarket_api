package org.cmarket.cmarket.web.map.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlaceSearchRequest {

    @NotNull(message = "category는 필수입니다.")
    private PlaceCategory category;

    @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "latitude는 90 이하여야 합니다.")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "longitude는 180 이하여야 합니다.")
    private Double longitude;

    @DecimalMin(value = "0.1", message = "radius는 0보다 커야 합니다.")
    private Double radius;

    @DecimalMin(value = "-90.0", message = "minLatitude는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "minLatitude는 90 이하여야 합니다.")
    private Double minLatitude;

    @DecimalMin(value = "-90.0", message = "maxLatitude는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "maxLatitude는 90 이하여야 합니다.")
    private Double maxLatitude;

    @DecimalMin(value = "-180.0", message = "minLongitude는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "minLongitude는 180 이하여야 합니다.")
    private Double minLongitude;

    @DecimalMin(value = "-180.0", message = "maxLongitude는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "maxLongitude는 180 이하여야 합니다.")
    private Double maxLongitude;

    private Boolean isRecommended;

    private Boolean is24Hours;

    private Boolean isEmergencyAvailable;

    private List<AnimalType> animalTypes;

    @Min(value = 0, message = "page는 0 이상이어야 합니다.")
    private Integer page;

    @Min(value = 1, message = "size는 1 이상이어야 합니다.")
    private Integer size;
}
