package org.cmarket.cmarket.web.map.dto;

import org.cmarket.cmarket.domain.map.app.dto.PetFriendlyPlaceImportCommand;

import java.util.List;

public record PublicAnimalExhibitionFetchResult(
        int totalCount,
        List<PetFriendlyPlaceImportCommand> places
) {
}
