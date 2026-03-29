package org.cmarket.cmarket.web.map.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cmarket.cmarket.domain.map.app.dto.PlaceReviewCreateCommand;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlaceReviewCreateRequest {

    @Min(value = 1, message = "rating은 1 이상이어야 합니다.")
    @Max(value = 5, message = "rating은 5 이하여야 합니다.")
    private Integer rating;

    @NotBlank(message = "content는 필수입니다.")
    @Size(max = 1000, message = "content는 1000자 이하여야 합니다.")
    private String content;

    @Size(max = 5, message = "imageUrls는 최대 5개까지 가능합니다.")
    private List<String> imageUrls;

    public PlaceReviewCreateCommand toCommand() {
        return PlaceReviewCreateCommand.builder()
                .rating(rating)
                .content(content)
                .imageUrls(imageUrls)
                .build();
    }
}
