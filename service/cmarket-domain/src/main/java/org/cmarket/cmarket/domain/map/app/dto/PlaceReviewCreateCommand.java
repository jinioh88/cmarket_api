package org.cmarket.cmarket.domain.map.app.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaceReviewCreateCommand {

    private Integer rating;
    private String content;
    private List<String> imageUrls;
}
