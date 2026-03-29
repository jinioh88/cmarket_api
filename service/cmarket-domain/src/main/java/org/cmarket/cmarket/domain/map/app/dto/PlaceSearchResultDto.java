package org.cmarket.cmarket.domain.map.app.dto;

import org.cmarket.cmarket.domain.profile.app.dto.PageResult;

public record PlaceSearchResultDto(
        PageResult<PlaceListItemDto> places
) {
}
