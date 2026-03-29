package org.cmarket.cmarket.web.admin.dto;

import lombok.Builder;
import lombok.Getter;
import org.cmarket.cmarket.domain.map.app.dto.AdminPlaceListItemDto;
import org.cmarket.cmarket.domain.map.model.PlaceCategory;
import org.cmarket.cmarket.domain.map.model.PlaceSourceType;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminPlaceListItemResponse {

    private Long id;
    private PlaceCategory category;
    private String name;
    private String address;
    private Boolean isRecommended;
    private PlaceSourceType sourceType;
    private LocalDateTime updatedAt;

    public static AdminPlaceListItemResponse fromDto(AdminPlaceListItemDto dto) {
        return AdminPlaceListItemResponse.builder()
                .id(dto.getId())
                .category(dto.getCategory())
                .name(dto.getName())
                .address(dto.getAddress())
                .isRecommended(dto.getIsRecommended())
                .sourceType(dto.getSourceType())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
