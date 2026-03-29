package org.cmarket.cmarket.web.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceDto;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;

@Getter
@NoArgsConstructor
public class AdminPlaceResponse {

    private Long id;
    private PlaceCategory category;
    private String name;
    private String address;
    private String phone;
    private String operatingHours;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private Boolean isRecommended;
    private HospitalDetailInfoResponse detail;

    public static AdminPlaceResponse fromDto(AdminPlaceDto dto) {
        AdminPlaceResponse response = new AdminPlaceResponse();
        response.id = dto.getId();
        response.category = dto.getCategory();
        response.name = dto.getName();
        response.address = dto.getAddress();
        response.phone = dto.getPhone();
        response.operatingHours = dto.getOperatingHours();
        response.imageUrl = dto.getImageUrl();
        response.latitude = dto.getLatitude();
        response.longitude = dto.getLongitude();
        response.isRecommended = dto.getIsRecommended();
        response.detail = HospitalDetailInfoResponse.fromDto(dto.getDetail());
        return response;
    }
}
