package org.cmarket.cmarket.web.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cmarket.cmarket.domain.community.app.dto.PostUpdateCommand;

import java.util.List;

/**
 * 게시글 수정 요청 DTO
 * 
 * 게시글 수정 시 필요한 모든 정보를 받습니다.
 */
@Getter
@NoArgsConstructor
public class PostUpdateRequest {
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 2, max = 50, message = "제목은 2자 이상 50자 이하여야 합니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 2, max = 1000, message = "내용은 2자 이상 1000자 이하여야 합니다.")
    private String content;
    
    @Size(max = 5, message = "이미지는 최대 5장까지 등록 가능합니다.")
    private List<String> imageUrls;
    
    /**
     * 웹 DTO를 앱 DTO로 변환
     * 
     * @return PostUpdateCommand
     */
    public PostUpdateCommand toCommand() {
        return PostUpdateCommand.builder()
                .title(this.title)
                .content(this.content)
                .imageUrls(this.imageUrls)
                .build();
    }
}

