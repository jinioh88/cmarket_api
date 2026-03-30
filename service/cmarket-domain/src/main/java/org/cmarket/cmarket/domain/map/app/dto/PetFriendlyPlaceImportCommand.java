package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@Builder
public class PetFriendlyPlaceImportCommand {

    private PlaceCategory category;
    private String externalPlaceId;
    private String name;
    private String address;
    private String phone;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String salesStatusCode;
    private String salesStatusName;
}
