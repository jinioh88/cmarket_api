package org.cmarket.cmarket.web.map.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceCommand;
import org.cmarket.cmarket.domain.map.model.AnimalType;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AdminPlaceRequest {

    @NotNull(message = "category는 필수입니다.")
    private PlaceCategory category;

    @NotBlank(message = "name은 필수입니다.")
    private String name;

    @NotBlank(message = "address는 필수입니다.")
    private String address;

    private String phone;

    private String operatingHours;

    private String imageUrl;

    @NotNull(message = "latitude는 필수입니다.")
    @DecimalMin(value = "-90.0", message = "latitude는 -90 이상이어야 합니다.")
    @DecimalMax(value = "90.0", message = "latitude는 90 이하여야 합니다.")
    private Double latitude;

    @NotNull(message = "longitude는 필수입니다.")
    @DecimalMin(value = "-180.0", message = "longitude는 -180 이상이어야 합니다.")
    @DecimalMax(value = "180.0", message = "longitude는 180 이하여야 합니다.")
    private Double longitude;

    private Boolean isRecommended;

    private Boolean is24Hours;

    private Boolean isEmergencyAvailable;

    private List<AnimalType> animalTypes;

    public AdminPlaceCommand toCommand() {
        return AdminPlaceCommand.builder()
                .category(category)
                .name(name)
                .address(address)
                .phone(phone)
                .operatingHours(operatingHours)
                .imageUrl(imageUrl)
                .latitude(latitude)
                .longitude(longitude)
                .isRecommended(isRecommended)
                .is24Hours(is24Hours)
                .isEmergencyAvailable(isEmergencyAvailable)
                .animalTypes(animalTypes)
                .build();
    }
}
